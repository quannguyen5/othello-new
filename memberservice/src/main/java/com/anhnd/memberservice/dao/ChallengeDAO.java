package com.anhnd.memberservice.dao;

import com.anhnd.memberservice.model.Challenge;
import com.anhnd.memberservice.model.Member;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public boolean createChallenge(Challenge challenge) {
        String sql = """
            insert into challenges(challenger_id, challenged_id, created_at, expires_at, status) values(?,?,NOW(),NOW() + INTERVAL 2 MINUTE,"PENDING");
        """;
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, challenge.getChallenger().getId());
            ps.setInt(2, challenge.getChallenged().getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
                    challenges.add(challenge);
                }
            }
            return challenges;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return challenges;
    }
}
