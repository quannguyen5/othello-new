package com.anhnd.matchservice.dao;

import com.anhnd.matchservice.model.Bot;
import com.anhnd.matchservice.model.Challenge;
import com.anhnd.matchservice.model.Match;
import com.anhnd.matchservice.model.Member;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            insert into `match`(white_to_black, challenge_id) values(?,?);
        """;
        try (Connection conn = this.getConnection();
             // Thêm Statement.RETURN_GENERATED_KEYS để lấy ID mới
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, match.getWhiteToBlack());
            ps.setInt(2, match.getChallenge().getId());

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

    /**
     * save match result
     */
    public boolean saveMatchResult(Match match) {
        String sql = "UPDATE `match` SET white_to_black = ? WHERE id = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, match.getWhiteToBlack());
            ps.setInt(2, match.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * getMatchesByMember
     */
    public List<Match> getMatchesByChallengeIds(List<Integer> challengeIds) {
        if (challengeIds.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT * FROM `match` WHERE challenge_id IN ("
        );

        for (int i = 0; i < challengeIds.size(); i++) {
            sqlBuilder.append("?");
            if (i < challengeIds.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");

        List<Match> matches = new ArrayList<>();

        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < challengeIds.size(); i++) {
                ps.setInt(i + 1, challengeIds.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Match match = new Match();
                    match.setId(rs.getInt("id"));
                    match.setWhiteToBlack(rs.getInt("white_to_black"));

                    // Chỉ thiết lập ID của challenge, không load đầy đủ challenge
                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("challenge_id"));
                    match.setChallenge(challenge);

                    matches.add(match);
                }
            }

            return matches;
        } catch (SQLException e) {
            e.printStackTrace();
            return matches;
        }
    }

    /**
     * Lấy tất cả trận đấu
     */
    public List<Match> getAllMatches() {
        String sql = "SELECT * FROM `match`";
        List<Match> matches = new ArrayList<>();

        try (Connection conn = this.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Match match = new Match();
                match.setId(rs.getInt("id"));
                match.setWhiteToBlack(rs.getInt("white_to_black"));

                Challenge challenge = new Challenge();
                challenge.setId(rs.getInt("challenge_id"));
                match.setChallenge(challenge);

                matches.add(match);
            }

            return matches;
        } catch (SQLException e) {
            e.printStackTrace();
            return matches;
        }
    }

    /**
     * Lấy thông tin match theo ID
     */
    public Match getMatchById(int matchId) {
        String sql = "SELECT * FROM `match` WHERE id = ?";

        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Match match = new Match();
                    match.setId(rs.getInt("id"));
                    match.setWhiteToBlack(rs.getInt("white_to_black"));

                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("challenge_id"));
                    match.setChallenge(challenge);

                    return match;
                }
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}