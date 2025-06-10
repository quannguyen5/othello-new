let userId = sessionStorage.getItem("currentUserId");
let pendingChallenges = [];

if (!userId || userId.length === 0) {
    console.error("User ID not found in sessionStorage");
}

const stompClient = new StompJs.Client({
    brokerURL: 'ws://memberservice.anhnd.vn/member-service-websocket',
    connectHeaders: {
        userId: userId
    },
    debug: function(str) {
        console.log("STOMP Debug: " + str);
    },
    // Thêm cấu hình retry
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
});

let isRedirecting = false;

stompClient.onConnect = function(frame) {
    console.log('Connected: ' + frame);

    // Đăng ký nhận tin nhắn về trận đấu đã được tạo
    stompClient.subscribe('/queue/match.' + userId, function(message) {
        try {
            const data = JSON.parse(message.body);
            console.log("Received match data:", data);

            if (data && data.id) {
                // Đặt cờ chuyển hướng
                isRedirecting = true;

                console.log("Redirecting to play-with-friend/" + data.id);

                // Thông báo cho người dùng
                showMatchCreatedNotification();

                // Chuyển hướng đến trang trận đấu với ID cụ thể
                setTimeout(() => {
                    window.location.href = `/play-with-friend/${data.id}`;
                }, 1500);
            } else {
                console.error("Received match message but no valid match ID was found:", data);
            }
        } catch (error) {
            console.error("Error processing match message:", error);
        }
    });

    // Đăng ký nhận tin nhắn thách đấu và phản hồi
    stompClient.subscribe('/queue/private.' + userId, function(message) {
        try {
            console.log("Received private message:", message.body);
            const data = JSON.parse(message.body);

            // Xử lý các loại tin nhắn khác nhau
            switch (data.type) {
                case "CHALLENGE_REQUEST":
                    console.log(`Received challenge from user ${data.senderId}`);
                    // Thêm thách đấu vào danh sách chờ
                    pendingChallenges.push(data);
                    // Cập nhật UI hiển thị thách đấu
                    updateChallengeNotifications();
                    break;

                case "CHALLENGE_ACCEPTED":
                    console.log(`User ${data.senderId} accepted your challenge`);
                    // Chỉ hiển thị thông báo, KHÔNG chuyển hướng
                    // Việc chuyển hướng sẽ được xử lý khi nhận được match ID
                    showChallengeAcceptedNotification(data.senderId);
                    break;

                case "CHALLENGE_DECLINED":
                    console.log(`User ${data.senderId} declined your challenge`);
                    showChallengeDeclinedNotification(data.senderId);
                    break;

                default:
                    console.log("Received unknown message type:", data.type);
            }
        } catch (error) {
            console.error("Error processing private message:", error);
        }
    });
};

// Hàm hiển thị thông báo trận đấu đã được tạo
function showMatchCreatedNotification() {
    const existingNotifications = document.querySelectorAll('.match-notification');
    existingNotifications.forEach(notif => notif.remove());

    const notification = document.createElement('div');
    notification.className = 'game-notification match-notification';
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.width = '280px';
    notification.style.padding = '16px';
    notification.style.backgroundColor = '#e3f2fd';
    notification.style.borderLeft = '4px solid #2196F3';
    notification.style.borderRadius = '8px';
    notification.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    notification.style.zIndex = '1000';

    notification.innerHTML = `
        <div class="notification-content" style="display: flex; flex-direction: column; align-items: center; text-align: center;">
            <p style="margin: 5px 0; color: #333; font-weight: 600; font-size: 15px;">Trận đấu đã được tạo!</p>
            <p style="margin: 5px 0; color: #333; font-size: 14px; font-weight: 500;">Đang chuyển đến màn hình trò chơi...</p>
        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification && notification.parentNode) {
            notification.remove();
        }
    }, 2000);
}

function createChallenge(memberId, selectedColor) {
    console.log("Creating challenge for member ID:", memberId, "with color:", selectedColor);

    if (!stompClient.connected) {
        console.error("STOMP client not connected");

        if (userId && userId.length > 0) {
            console.log("Attempting to reconnect...");
            stompClient.activate();
            return;
        } else {
            alert("Cannot create challenge: User ID not found");
            return;
        }
    }

    stompClient.publish({
        destination: "/app/challenge",
        body: JSON.stringify({
            'senderId': userId,
            'receiverId': memberId,
            'type': 'CHALLENGE_REQUEST',
            'senderColor': selectedColor || 'black' // Mặc định là đen nếu không chọn
        })
    });
}

stompClient.onWebSocketError = function(error) {
    console.error('WebSocket Error:', error);
};

stompClient.onStompError = function(frame) {
    console.error('STOMP Error:', frame.headers['message']);
    console.error('Additional details:', frame.body);
};

if (userId && userId.length > 0) {
    console.log("Activating STOMP connection for user:", userId);
    stompClient.activate();
}

function disconnect() {
    if (stompClient.connected) {
        stompClient.deactivate();
        console.log("Disconnected");
    }
}

document.addEventListener('DOMContentLoaded', function() {
    console.log("Page loaded, STOMP connection setup complete");
});

function updateChallengeNotifications() {
    const container = document.getElementById('challenge-notifications');
    if (!container) {
        // Create a modal if it doesn't exist yet
        createChallengeModal();
        return;
    }

    // Clear existing notifications
    container.innerHTML = '';

    if (pendingChallenges.length === 0) {
        container.innerHTML = '<p>No pending challenges</p>';
        return;
    }

    // Add each challenge to the container
    pendingChallenges.forEach((challenge, index) => {
        const challengeDiv = document.createElement('div');
        challengeDiv.className = 'challenge-item';

        // Hiển thị thông tin màu quân nếu có
        const colorInfo = challenge.senderColor ?
            `<p>Thách đấu với màu: <strong>${challenge.senderColor === 'white' ? 'Trắng' : 'Đen'}</strong></p>` : '';

        challengeDiv.innerHTML = `
            <p>Người chơi ${challenge.senderId} thách đấu bạn!</p>
            ${colorInfo}
            <div class="challenge-actions">
                <button onclick="acceptChallenge(${index})">Chấp nhận</button>
                <button onclick="declineChallenge(${index})">Từ chối</button>
            </div>
        `;
        container.appendChild(challengeDiv);
    });

    // Show the modal
    document.getElementById('challenge-modal').style.display = 'block';
}

function createChallengeModal() {
    console.log("Đang tạo modal...");

    // Create modal container
    const modal = document.createElement('div');
    modal.id = 'challenge-modal';
    modal.className = 'modal';
    modal.style.display = 'none';

    // Create modal content
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close" onclick="document.getElementById('challenge-modal').style.display='none'">&times;</span>
            <h2>Lời mời thách đấu</h2>
            <div id="challenge-notifications"></div>
        </div>
    `;

    // Add modal styles
    const style = document.createElement('style');
    style.textContent = `
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.4);
        }
        .modal-content {
            background-color: white;
            margin: 15% auto;
            padding: 20px;
            border-radius: 5px;
            width: 300px;
            max-width: 80%;
        }
        .close {
            color: #aaa;
            float: right;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
        }
        .challenge-item {
            padding: 10px;
            margin-bottom: 10px;
            border: 1px solid #eee;
            border-radius: 5px;
        }
        .challenge-actions {
            display: flex;
            justify-content: space-between;
            margin-top: 10px;
        }
        .challenge-actions button {
            padding: 5px 10px;
            border: none;
            border-radius: 3px;
            cursor: pointer;
        }
        .challenge-actions button:first-child {
            background-color: #4CAF50;
            color: white;
        }
        .challenge-actions button:last-child {
            background-color: #f44336;
            color: white;
        }
    `;
    // Add to document
    document.head.appendChild(style);
    document.body.appendChild(modal);

    // Update the notifications
    updateChallengeNotifications();
}

function acceptChallenge(index) {
    const challenge = pendingChallenges[index];
    console.log(`Accepting challenge from user ${challenge.senderId}`);

    // Send accept message via WebSocket
    stompClient.publish({
        destination: "/app/challenge-response",
        body: JSON.stringify({
            'senderId': userId,
            'receiverId': challenge.senderId,
            'type': 'CHALLENGE_ACCEPTED',
            'senderColor': challenge.senderColor
        })
    });

    // Remove from pending challenges
    pendingChallenges.splice(index, 1);

    // Hiển thị thông báo đang chờ trận đấu
    showWaitingForMatchNotification();

    // Hide modal
    document.getElementById('challenge-modal').style.display = 'none';
}

function showWaitingForMatchNotification() {
    const notification = document.createElement('div');
    notification.className = 'game-notification waiting-notification';
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.width = '280px';
    notification.style.padding = '16px';
    notification.style.backgroundColor = '#fff9c4';
    notification.style.borderLeft = '4px solid #fbc02d';
    notification.style.borderRadius = '8px';
    notification.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    notification.style.zIndex = '1000';

    notification.innerHTML = `
        <div class="notification-content" style="display: flex; flex-direction: column; align-items: center; text-align: center;">
            <p style="margin: 5px 0; color: #333; font-weight: 600; font-size: 15px;">Đã chấp nhận lời thách đấu!</p>
            <p style="margin: 5px 0; color: #333; font-size: 14px; font-weight: 500;">Đang chuẩn bị trận đấu...</p>
        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification && notification.parentNode && !isRedirecting) {
            notification.remove();
        }
    }, 5000);
}

function declineChallenge(index) {
    const challenge = pendingChallenges[index];
    console.log(`Declining challenge from user ${challenge.senderId}`);

    // Send decline message via WebSocket
    stompClient.publish({
        destination: "/app/challenge-response",
        body: JSON.stringify({
            'senderId': userId,
            'receiverId': challenge.senderId,
            'type': 'CHALLENGE_DECLINED',
            'senderColor': challenge.senderColor
        })
    });

    // Remove from pending challenges
    pendingChallenges.splice(index, 1);

    // Update the UI
    updateChallengeNotifications();
}

function showChallengeAcceptedNotification(userId) {
    const existingNotifications = document.querySelectorAll('.accepted-notification');
    existingNotifications.forEach(notif => notif.remove());

    const notification = document.createElement('div');
    notification.className = 'game-notification accepted-notification';

    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.width = '280px';
    notification.style.padding = '16px';
    notification.style.backgroundColor = '#e8f5e9';
    notification.style.borderLeft = '4px solid #4CAF50';
    notification.style.borderRadius = '8px';
    notification.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    notification.style.zIndex = '1000';

    notification.innerHTML = `
        <div class="notification-content" style="display: flex; flex-direction: column; align-items: center; text-align: center;">
            <p style="margin: 5px 0; color: #333; font-weight: 600; font-size: 15px;">Người chơi ${userId} đã chấp nhận lời thách đấu của bạn!</p>
            <p style="margin: 5px 0; color: #333; font-size: 14px; font-weight: 500;">Đang chuẩn bị trận đấu...</p>
        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification && notification.parentNode && !isRedirecting) {
            notification.remove();
        }
    }, 5000);
}

function showChallengeDeclinedNotification(userId) {
    const existingNotifications = document.querySelectorAll('.declined-notification');
    existingNotifications.forEach(notif => notif.remove());

    const notification = document.createElement('div');
    notification.className = 'game-notification declined-notification';

    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.width = '280px';
    notification.style.padding = '16px';
    notification.style.backgroundColor = '#ffebee';
    notification.style.borderLeft = '4px solid #f44336';
    notification.style.borderRadius = '8px';
    notification.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    notification.style.zIndex = '1000';

    notification.innerHTML = `
        <div class="notification-content" style="display: flex; flex-direction: column; align-items: center; text-align: center;">
            <p style="margin: 5px 0; color: #333; font-weight: 600; font-size: 15px;">Người chơi ${userId} đã từ chối lời thách đấu của bạn!</p>
            <button onclick="this.parentElement.parentElement.remove()" style="margin-top: 10px; padding: 6px 18px; background-color: #f44336; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: 500;">Đóng</button>
        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification && notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}