let userId = sessionStorage.getItem("currentUserId");

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
            console.log(`Received challenge from user ${data.senderId}`);
            showChallengeNotification(data);
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