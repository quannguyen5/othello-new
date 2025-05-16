package com.anhnd.memberservice.controller;

import com.anhnd.memberservice.dao.ChallengeDAO;
import com.anhnd.memberservice.dao.MemberDAO;
import com.anhnd.memberservice.model.Challenge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @MessageMapping("/challenge-response")
    public void challengeResponse(Request request) {
        System.out.println(String.format("%s response from %d to %d",
                request.getType(),
                request.getSenderId(),
                request.getReceiverId()));

        messagingTemplate.convertAndSend(
                "/queue/private." + request.getReceiverId(),
                request
        );

        if ("CHALLENGE_ACCEPTED".equals(request.getType())) {
            Challenge challenge = challengeDAO.findPendingChallenge(
                    request.getReceiverId(),
                    request.getSenderId()
            );

            if (challenge != null) {
                challenge.setStatus("ACCEPTED");
                challengeDAO.updateChallengeStatus(challenge);
                System.out.println(request.getReceiverId());
                List<Challenge> declineChallenge = challengeDAO.findAllPendindChallengesById(request.getSenderId());
                for (Challenge otherChallenge : declineChallenge) {

                    otherChallenge.setStatus("DECLINED");
                    challengeDAO.updateChallengeStatus(otherChallenge);
                    Request declineRequest = new Request(
                            request.getSenderId(),
                            otherChallenge.getChallenger().getId(),
                            "CHALLENGE_DECLINED"
                    );

                    messagingTemplate.convertAndSend(
                            "/queue/private." + otherChallenge.getChallenger().getId(),
                            declineRequest
                    );

                }
            }
        } else if ("CHALLENGE_DECLINED".equals(request.getType())) {
            Challenge challenge = challengeDAO.findPendingChallenge(
                    request.getReceiverId(),
                    request.getSenderId()
            );

            if (challenge != null) {
                challenge.setStatus("DECLINED");
                challengeDAO.updateChallengeStatus(challenge);
            }
        }
    }
}
