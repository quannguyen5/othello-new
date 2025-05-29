package com.anhnd.matchservice.dao;

import com.anhnd.matchservice.model.Match;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class MatchDAO extends MatchServiceDAO {
    public MatchDAO() {
        super();
    }

    /**
     * create match và trả về match với ID mới
     */
    public Match createMatch(Match match) {
        String sql = """
            insert into `match`(is_white_requester, white_to_black, challenge_id) values(?,?,?);
        """;
        try (Connection conn = this.getConnection();
             // Thêm Statement.RETURN_GENERATED_KEYS để lấy ID mới
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, match.getIsWhiteRequester());
            ps.setInt(2, match.getWhiteToBlack());
            ps.setInt(3, match.getChallenge().getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Lấy ID được sinh tự động
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        match.setId(newId);
                        return match;
                    } else {
                        throw new SQLException("Tạo match thất bại, không lấy được ID.");
                    }
                }
            } else {
                throw new SQLException("Tạo match thất bại, không có dòng nào được thêm vào.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}