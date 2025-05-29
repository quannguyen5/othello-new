package com.anhnd.matchservice.controller;

import com.anhnd.matchservice.dao.MatchDAO;
import com.anhnd.matchservice.model.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/match-service/api/")
public class MatchServiceController {
    @Autowired
    private MatchDAO matchDAO;

    @PostMapping("create-match")
    public Match createMatch(@RequestBody() Match match) {
        return matchDAO.createMatch(match);
    }
}
