package com.anhnd.memberservice.controller;

import com.anhnd.memberservice.dao.ChallengeDAO;
import com.anhnd.memberservice.dao.MemberDAO;
import com.anhnd.memberservice.model.Challenge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

class Request {
    private int senderId;
    private int receiverId;
    private String type;

    public Request() {
    }

    public Request(int senderId, int receiverId, String type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

@RestController
public class WebSocketHandler {

    @Autowired
    private ChallengeDAO challengeDAO;

    @Autowired
    private MemberDAO memberDAO;

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/challenge")
    public void challenge(Request request) {
        System.out.println(String.format("from %d to %d",
                request.getSenderId(),
                request.getReceiverId()));

        Challenge challenge = new Challenge();
        challenge.setChallenger(memberDAO.findById(request.getSenderId()));
        challenge.setChallenged(memberDAO.findById(request.getReceiverId()));
        boolean isCreated = challengeDAO.createChallenge(challenge);

        System.out.println(isCreated);

        messagingTemplate.convertAndSend(
                "/queue/private." + request.getReceiverId(),
                new Request(
                        request.getSenderId(),
                        request.getReceiverId(),
                        "CHALLENGE_REQUEST"
                )
        );

    }
}
