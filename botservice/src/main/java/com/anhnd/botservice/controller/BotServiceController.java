package com.anhnd.botservice.controller;

import com.anhnd.botservice.dao.BotDAO;
import com.anhnd.botservice.model.Board;
import com.anhnd.botservice.model.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("bot-service/api")
public class BotServiceController {

    @Autowired
    private BotDAO botDAO;

    @PostMapping("/make-move")
    public Board getBotMove(@RequestBody Board board) {
        try {
            Bot bot = botDAO.getBotByAlgorithm(board.getAlgorithm());

            if (bot == null) {
                throw new IllegalArgumentException("Không tìm thấy bot với thuật toán: " + board.getAlgorithm());
            }

            return bot.makeMove(board);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
