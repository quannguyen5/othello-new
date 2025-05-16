package com.anhnd.resultservice.controller;

import com.anhnd.resultservice.dao.ResultDAO;
import com.anhnd.resultservice.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.*;

@RestController
@RequestMapping("/result-service/api")
public class ResultServiceController {

    @Autowired
    private ResultDAO resultDAO;

    @PostMapping("/save-result")
    public boolean saveResult(@RequestBody Result result) {

        return resultDAO.saveResult(result);
    }

    @GetMapping("/get-list-result")
    public List<Result> getResultByMemberId(@RequestParam("memberId") int memberId) throws SQLException {
        return resultDAO.getResultByMemberId(memberId);
    }

    @GetMapping("/get-list-result-to-opponent")
    public List<Result> getResultByTypeAndPlayerId(
            @RequestParam("memberId") int memberId,
            @RequestParam("opponentId") int opponentId,
            @RequestParam("type") int resultType
    ) throws SQLException {
        List<Result> listResult = resultDAO.getResultByMemberId(memberId);
        List<Result> listResultType = new ArrayList<>();
        for(Result result : listResult){
            if(result.getBot().getId() == 0) {
                // win
                if(resultType == 1) {
                    if((result.getMemberA().getId() == memberId && result.getMemberB().getId() == opponentId && result.getResAtoB() == 1) ||
                            result.getMemberA().getId() == opponentId && result.getMemberB().getId() == memberId && result.getResAtoB() == 0) {
                        listResultType.add(result);
                    }
                } else if(resultType == 0) {
                    if((result.getMemberA().getId() == memberId && result.getMemberB().getId() == opponentId && result.getResAtoB() == 0) ||
                            result.getMemberA().getId() == opponentId && result.getMemberB().getId() == memberId && result.getResAtoB() == 1) {
                        listResultType.add(result);
                    }
                } else {
                    if(resultType == 2) {
                        if(result.getResAtoB() == 2) {
                            listResultType.add(result);
                        }
                    }
                }
            }
        }
        return listResultType;
    }
}
