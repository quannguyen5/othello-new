package com.anhnd.memberservice.controller;

import com.anhnd.memberservice.dao.ChallengeDAO;
import com.anhnd.memberservice.dao.MemberDAO;
import com.anhnd.memberservice.model.Challenge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

class Request {
    private int senderId;
    private int receiverId;
    private String type;
    private String senderColor;

    public Request() {
    }

    public Request(int senderId, int receiverId, String type, String senderColor) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.senderColor = senderColor;
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

    public String getSenderColor() {
        return senderColor;
    }

    public void setSenderColor(String senderColor) {
        this.senderColor = senderColor;
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

    @PostMapping("/member-service/api/create-challenge")
    public Challenge createChallenge(@RequestBody() Challenge challenge) {
        return challengeDAO.createChallenge(challenge);
    }

    @MessageMapping("/challenge")
    public void challenge(Request request) {
        System.out.println(String.format("from %d to %d type %s color %S",
                request.getSenderId(),
                request.getReceiverId(),
                request.getType(),
                request.getSenderColor())
        );

        Challenge challenge = new Challenge();
        challenge.setChallenger(memberDAO.findById(request.getSenderId()));
        challenge.setChallenged(memberDAO.findById(request.getReceiverId()));
        challenge.setStatus("PENDING");
        challenge.setWithBot(0);

        //hardcode
        if(request.getSenderColor().equalsIgnoreCase("WHITE")) {
            challenge.setIsWhiteRequester(1);
        } else {
            challenge.setIsWhiteRequester(0);
        }

        Challenge isCreated = challengeDAO.createChallenge(challenge);

        messagingTemplate.convertAndSend(
                "/queue/private." + request.getReceiverId(),
                new Request(
                        request.getSenderId(),
                        request.getReceiverId(),
                        "CHALLENGE_REQUEST",
                        request.getSenderColor()
                )
        );
    }

    @MessageMapping("/challenge-response")
    public void challengeResponse(Request request) {
        System.out.println(String.format("%s response from %d to %d color %s",
                request.getType(),
                request.getSenderId(),
                request.getReceiverId(),
                request.getSenderColor()));

        if ("CHALLENGE_ACCEPTED".equals(request.getType())) {
            messagingTemplate.convertAndSend(
                    "/queue/private." + request.getReceiverId(),
                    request
            );

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
                            "CHALLENGE_DECLINED",
                            request.getSenderColor()
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
