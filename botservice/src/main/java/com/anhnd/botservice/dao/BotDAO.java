package com.anhnd.botservice.dao;

import com.anhnd.botservice.model.Bot;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class BotDAO extends BotServiceDAO {
    public BotDAO() {
        super();
    }

    public Bot getBotByAlgorithm(String algorithm) throws SQLException {
        String sql = "select * from bot where algorithm = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, algorithm);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bot bot = new Bot();
                    bot.setId(rs.getInt("id"));
                    bot.setAlgorithm(rs.getString("algorithm"));
                    return bot;
                }
            }
        }
        return null;
    }
}
