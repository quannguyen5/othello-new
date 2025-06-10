package com.anhnd.memberservice.dao;

import com.anhnd.memberservice.model.Member;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemberDAO extends MemberServiceDAO {

    public MemberDAO() {
        super();
    }

    /**
     * Xác thực đăng nhập
     */
    public Member processLogin(Member member) {
        String sql = "SELECT * FROM Member WHERE username = ? AND password = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getUsername());
            ps.setString(2, member.getPassword());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    member = new Member();
                    member.setId(rs.getInt("id"));
                    member.setUsername(rs.getString("username"));
                    member.setPassword(rs.getString("password"));
                    member.setEmail(rs.getString("email"));
                    member.setElo(rs.getInt("elo"));
                    member.setStatus(1);
                    updateMemberStatus(member);
                    return member;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm member theo username
     */
    public Member findByUsername(String username) {
        String sql = "SELECT * FROM Member WHERE username = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getInt("id"));
                    member.setUsername(rs.getString("username"));
                    member.setPassword(rs.getString("password"));
                    member.setEmail(rs.getString("email"));
                    member.setElo(rs.getInt("elo"));
                    return member;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm member theo id
     */
    public Member findById(int memberId) {
        String sql = "SELECT * FROM Member WHERE id = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getInt("id"));
                    member.setUsername(rs.getString("username"));
                    member.setPassword(rs.getString("password"));
                    member.setEmail(rs.getString("email"));
                    member.setElo(rs.getInt("elo"));
                    return member;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * update user status
     */
    public boolean updateMemberStatus(Member member) throws SQLException {
        String sql = "UPDATE Member SET status = ? WHERE id = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, member.getStatus());
            ps.setInt(2, member.getId());
            // execute query
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * lấy list friend
     */
    public List<Member> getListFriend(int memberId) throws SQLException {
        String sql = """
            SELECT m2.*
            FROM friendinvitation f
            JOIN Member m1 ON (f.requestId = m1.id OR f.receiveId = m1.id)
            JOIN Member m2 ON (
                (m2.id = f.requestId OR m2.id = f.receiveId)
                AND m2.id != m1.id
            )
            WHERE f.status = 'ACCEPTED'
            AND m1.id = ?;
        """;
        List<Member> listFriend = new ArrayList<>();
        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getInt("id"));
                    member.setUsername(rs.getString("username"));
                    member.setPassword(rs.getString("password"));
                    member.setEmail(rs.getString("email"));
                    member.setElo(rs.getInt("elo"));
                    member.setStatus(rs.getInt("status"));
                    listFriend.add(member);
                }
            }
        }
        return listFriend;
    }
}