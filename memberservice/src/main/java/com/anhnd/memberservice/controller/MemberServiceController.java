package com.anhnd.memberservice.controller;

import com.anhnd.memberservice.dao.ChallengeDAO;
import com.anhnd.memberservice.dao.MemberDAO;
import com.anhnd.memberservice.model.Challenge;
import com.anhnd.memberservice.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/member-service/api")
public class MemberServiceController {

    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private ChallengeDAO challengeDAO;

    @PostMapping("/login")
    public Member processLogin(@RequestBody Member member) {
        return memberDAO.processLogin(member);
    }

    @PostMapping("/logout")
    public boolean processLogout(@RequestBody Member member) throws SQLException {
        member.setStatus(0);
        return memberDAO.updateMemberStatus(member);
    }

    @GetMapping("/get-member")
    public Member getMember(@RequestParam("memberId") int memberId) {
        return memberDAO.findById(memberId);
    }

    @GetMapping("/list-friend")
    public List<Member> getListFriend(@RequestParam("memberId") int memberId) throws SQLException {
        Member member = memberDAO.findById(memberId);
        List<Member> friends = memberDAO.getListFriend(memberId);
        friends.add(member);
        friends.sort(Comparator.comparing(Member::getElo).reversed());
        return friends;
    }
    @GetMapping("/get-challenges-by-member")
    public List<Challenge> getChallengesByMember(@RequestParam("memberId") int memberId) {
        return challengeDAO.getChallengesByMember(memberId);
    }

    @GetMapping("/get-challenges-between-members")
    public List<Challenge> getChallengesBetweenMembers(
            @RequestParam("memberId") int memberId,
            @RequestParam("opponentId") int opponentId) {
        return challengeDAO.getChallengesBetweenMembers(memberId, opponentId);
    }

}
