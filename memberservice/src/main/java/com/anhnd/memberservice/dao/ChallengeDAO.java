package com.anhnd.memberservice.dao;

import com.anhnd.memberservice.model.Bot;
import com.anhnd.memberservice.model.Challenge;
import com.anhnd.memberservice.model.Member;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChallengeDAO extends MemberServiceDAO{
    public ChallengeDAO() {
        super();
    }

    /**
     * create challenge
     */
    public Challenge createChallenge(Challenge challenge) {
        String sql = """
        insert into challenges(challenger_id, challenged_id, created_at, expires_at, status, with_bot, is_white_requester) 
        values(?,?,NOW(),NOW() + INTERVAL 2 MINUTE,?,?,?);
    """;

        try (Connection conn = this.getConnection();
             // Thêm Statement.RETURN_GENERATED_KEYS ở đây
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, challenge.getChallenger().getId());
            if(challenge.getWithBot() == 1) {
                ps.setInt(2, challenge.getBot().getId());
            } else {
                ps.setInt(2, challenge.getChallenged().getId());
            }
            ps.setString(3, challenge.getStatus());
            ps.setInt(4, challenge.getWithBot());
            ps.setInt(5, challenge.getIsWhiteRequester());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        challenge.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Tạo challenge thất bại, không lấy được ID.");
                    }
                }
                return challenge;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find a pending challenge between two members
     */
    public Challenge findPendingChallenge(int challengerId, int challengedId) {
        String sql = "SELECT * FROM challenges WHERE challenger_id = ? AND challenged_id = ? AND status = 'PENDING'";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challengerId);
            ps.setInt(2, challengedId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("id"));

                    Member challenger = new Member();
                    challenger.setId(rs.getInt("challenger_id"));
                    challenge.setChallenger(challenger);

                    Member challenged = new Member();
                    challenged.setId(rs.getInt("challenged_id"));
                    challenge.setChallenged(challenged);

                    challenge.setStatus(rs.getString("status"));

                    return challenge;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update challenge status
     */
    public boolean updateChallengeStatus(Challenge challenge) {
        String sql = "UPDATE challenges SET status = ? WHERE id = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, challenge.getStatus());
            ps.setInt(2, challenge.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * get all pending by id
     */
    public List<Challenge> findAllPendindChallengesById(int receiveId) {
        String sql = "SELECT * FROM challenges WHERE status = 'PENDING' and challenged_id = ?";
        List<Challenge> challenges = new ArrayList<>();
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receiveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("id"));
                    Member challenger = new Member();
                    challenger.setId(rs.getInt("challenger_id"));
                    challenge.setChallenger(challenger);
                    Member challenged = new Member();
                    challenged.setId(rs.getInt("challenged_id"));
                    challenge.setChallenged(challenged);
                    challenge.setStatus(rs.getString("status"));
                    challenge.setIsWhiteRequester(rs.getInt("is_white_requester"));
                    challenges.add(challenge);
                }
            }
            return challenges;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return challenges;
    }

    public List<Challenge> getChallengesByMember(int memberId) {
        String sql = """
        SELECT * FROM challenges 
        WHERE (challenger_id = ? OR challenged_id = ?)
        AND status = 'ACCEPTED'
    """;

        List<Challenge> challenges = new ArrayList<>();

        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ps.setInt(2, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Challenge challenge = mapResultSetToChallenge(rs);
                    challenges.add(challenge);
                }
            }

            return challenges;
        } catch (SQLException e) {
            e.printStackTrace();
            return challenges;
        }
    }

    public List<Challenge> getChallengesBetweenMembers(int memberId, int opponentId) {
        String sql = """
        SELECT * FROM challenges
        WHERE ((challenger_id = ? AND challenged_id = ?) 
               OR (challenger_id = ? AND challenged_id = ?))
        AND status = 'ACCEPTED'
    """;

        List<Challenge> challenges = new ArrayList<>();

        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ps.setInt(2, opponentId);
            ps.setInt(3, opponentId);
            ps.setInt(4, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Challenge challenge = mapResultSetToChallenge(rs);
                    challenges.add(challenge);
                }
            }

            return challenges;
        } catch (SQLException e) {
            e.printStackTrace();
            return challenges;
        }
    }

    private Challenge mapResultSetToChallenge(ResultSet rs) throws SQLException {
        Challenge challenge = new Challenge();
        challenge.setId(rs.getInt("id"));

        Member challenger = new Member();
        challenger.setId(rs.getInt("challenger_id"));
        challenge.setChallenger(challenger);

        int withBot = rs.getInt("with_bot");
        challenge.setWithBot(withBot);

        if (withBot == 1) {
            Bot bot = new Bot();
            bot.setId(rs.getInt("bot_id"));
            challenge.setBot(bot);
        } else {
            Member challenged = new Member();
            challenged.setId(rs.getInt("challenged_id"));
            challenge.setChallenged(challenged);
        }

        challenge.setIsWhiteRequester(rs.getInt("is_white_requester"));
        challenge.setCreated_at(rs.getTimestamp("created_at"));
        challenge.setExpires_at(rs.getTimestamp("expires_at"));
        challenge.setStatus(rs.getString("status"));

        return challenge;
    }

}
