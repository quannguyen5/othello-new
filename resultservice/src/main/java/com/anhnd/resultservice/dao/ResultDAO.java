package com.anhnd.resultservice.dao;

import com.anhnd.resultservice.model.Bot;
import com.anhnd.resultservice.model.Member;
import com.anhnd.resultservice.model.Result;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class ResultDAO extends ResultServiceDAO {
    public ResultDAO() {
        super();
    }

    public boolean saveResult(Result result) {
        String sqlResult = "INSERT INTO Result (idMemberA, idMemberB, idBot, resAtoB, resAtoBot, created_at) VALUES (?, NULL, ?, NULL, ?, NOW())";

        Connection conn = null;
        PreparedStatement pstmtResult = null;

        try {
            conn = this.getConnection();
            conn.setAutoCommit(false);

            pstmtResult = conn.prepareStatement(sqlResult);
            pstmtResult.setInt(1, result.getMemberA().getId());
            pstmtResult.setInt(2, result.getBot().getId());
            pstmtResult.setInt(3, result.getResAToBot());

            int resultRowsAffected = pstmtResult.executeUpdate();

            conn.commit();

            return resultRowsAffected > 0;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }

            System.err.println("Error saving game result: " + e.getMessage());
            e.printStackTrace();

            return false;
        } finally {
            try {
                if (pstmtResult != null) pstmtResult.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database resources: " + e.getMessage());
            }
        }
    }

    /**
     * get list result by memberId
     */
    public List<Result> getResultByMemberId(int memberId) throws SQLException {
        String sql = "SELECT * FROM result WHERE idMemberA = ? or idMemberB = ?";
        List<Result> listResult = new ArrayList<Result>();
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Result res = new Result();
                    res.setId(rs.getInt("id"));

                    Member memberA = new Member();
                    Member memberB = new Member();
                    memberA.setId(rs.getInt("idMemberA"));
                    memberB.setId(rs.getInt("idMemberB"));
                    res.setMemberA(memberA);
                    res.setMemberB(memberB);

                    Bot bot = new Bot();
                    bot.setId(rs.getInt("idBot"));
                    res.setBot(bot);

                    res.setResAtoB(rs.getInt("resAtoB"));
                    res.setResAToBot(rs.getInt("resAtoBot"));
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        res.setCreatedAt(timestamp.toLocalDateTime());
                    }

                    listResult.add(res);
                }
            }
        }
        return listResult;
    }
}
