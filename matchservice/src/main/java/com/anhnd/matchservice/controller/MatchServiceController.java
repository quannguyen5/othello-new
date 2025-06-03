package com.anhnd.matchservice.controller;

import com.anhnd.matchservice.dao.MatchDAO;
import com.anhnd.matchservice.model.Match;
import com.anhnd.matchservice.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.MatchResult;

@RestController
@RequestMapping("/match-service/api/")
public class MatchServiceController {
    @Autowired
    private MatchDAO matchDAO;

    @PostMapping("create-match")
    public Match createMatch(@RequestBody() Match match) {
        return matchDAO.createMatch(match);
    }

    @PostMapping("save-match-result")
    public boolean saveMatchResult(@RequestBody Match match) {
        return matchDAO.saveMatchResult(match);
    }

    @GetMapping("get-all-matches")
    public List<Match> getAllMatches() {
        return matchDAO.getAllMatches();
    }

    @GetMapping("get-match-by-id")
    public Match getMatchById(@RequestParam("matchId") int matchId) {
        return matchDAO.getMatchById(matchId);
    }

    @PostMapping("get-matches-by-challenge-ids")
    public List<Match> getMatchesByChallengeIds(@RequestBody List<Integer> challengeIds) {
        return matchDAO.getMatchesByChallengeIds(challengeIds);
    }


}
