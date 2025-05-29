let userId = sessionStorage.getItem("currentUserId");
let pendingChallenges = [];

if (!userId || userId.length === 0) {
    console.error("User ID not found in sessionStorage");
}

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8081/member-service-websocket',
    connectHeaders: {
        userId: userId
    },
    debug: function(str) {
        console.log("STOMP Debug: " + str);
    }
});

stompClient.onConnect = function(frame) {
    console.log('Connected: ' + frame);

    stompClient.subscribe('/queue/private.' + userId, function(message) {
        console.log("Received private message:", message.body);
        const data = JSON.parse(message.body);
        if (data.type === "CHALLENGE_REQUEST") {
            console.log(`Received challenge from user ${data.senderId}`)
            // Add challenge to pending list
            pendingChallenges.push(data);

            // Update UI to show challenges
            updateChallengeNotifications();
        } else if (data.type === "CHALLENGE_ACCEPTED") {
            console.log(`User ${data.senderId} accepted your challenge`);
            showChallengeAcceptedNotification(data.senderId);
            // Redirect to game page
            setTimeout(() => {
                window.location.href = `/play-with-friend`;
            }, 2000);
        }
        else if (data.type === "CHALLENGE_DECLINED") {
            console.log(`User ${data.senderId} declined your challenge`);
            showChallengeDeclinedNotification(data.senderId);
        }

    });
};


function showChallengeNotification(challenge) {
    const notification = `User ${challenge.senderId} challenged you to a game!`;
    alert(`User ${challenge.senderId} challenged you to a game!`);
}


function createChallenge(memberId) {
    console.log("Creating challenge for member ID:", memberId);

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

    console.log("created " + userId)

    stompClient.publish({
        destination: "/app/challenge",
        body: JSON.stringify({
            'senderId': userId,
            'receiverId': memberId,
            'type': 'CHALLENGE_REQUEST'
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
        challengeDiv.innerHTML = `
            <p>User ${challenge.senderId} challenged you!</p>
            <div class="challenge-actions">
                <button onclick="acceptChallenge(${index})">Accept</button>
                <button onclick="declineChallenge(${index})">Decline</button>
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
    console.log("rep")

    // Create modal content
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close" onclick="document.getElementById('challenge-modal').style.display='none'">&times;</span>
            <h2>Challenge Invitations</h2>
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
            'type': 'CHALLENGE_ACCEPTED'
        })
    });

    // Remove from pending challenges
    pendingChallenges.splice(index, 1);

    // Redirect to game page
    window.location.href = `/play-with-friend`;

    // Hide modal
    document.getElementById('challenge-modal').style.display = 'none';
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
            'type': 'CHALLENGE_DECLINED'
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
            <p style="margin: 5px 0; color: #333; font-size: 14px; font-weight: 500;">Đang chuyển đến màn hình trò chơi...</p>
        </div>
    `;
    // alert(`Người chơi ${userId} đã chấp nhận lời thách đấu của bạn!!`);
    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification && notification.parentNode) {
            notification.remove();
        }
    }, 2000);
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
    // alert(`Người chơi ${userId} đã từ chối lời thách đấu của bạn!!`);
    setTimeout(() => {
        if (notification && notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}