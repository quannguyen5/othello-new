package com.anhnd.memberservice.dao;

import com.anhnd.memberservice.model.Challenge;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
