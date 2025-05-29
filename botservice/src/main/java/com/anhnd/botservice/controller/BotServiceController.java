package com.anhnd.botservice.controller;

import com.anhnd.botservice.dao.BotDAO;
import com.anhnd.botservice.model.Board;
import com.anhnd.botservice.model.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("bot-service/api")
public class BotServiceController {

    @Autowired
    private BotDAO botDAO;

    @GetMapping("get-bot")
    public Bot getBot(@RequestParam("algorithm") String algorithm) throws SQLException {
        return botDAO.getBotByAlgorithm(algorithm);
    }

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
