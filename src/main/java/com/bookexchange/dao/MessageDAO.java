package com.bookexchange.dao;

import com.bookexchange.model.Message;
import com.bookexchange.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    
   public boolean sendMessage(Message m) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, listing_id, subject, content) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getSenderId());
            ps.setInt(2, m.getReceiverId());
            if (m.getListingId() != null && m.getListingId() > 0) {
                ps.setInt(3, m.getListingId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, m.getSubject());
            ps.setString(5, m.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Message> getInboxMessages(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u1.name as sender_name, u2.name as receiver_name, l.title as listing_title " +
                     "FROM messages m " +
                     "LEFT JOIN users u1 ON m.sender_id = u1.user_id " +
                     "LEFT JOIN users u2 ON m.receiver_id = u2.user_id " +
                     "LEFT JOIN listings l ON m.listing_id = l.listing_id " +
                     "WHERE m.receiver_id = ? " +
                     "ORDER BY m.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    public List<Message> getOutboxMessages(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u1.name as sender_name, u2.name as receiver_name, l.title as listing_title " +
                     "FROM messages m " +
                     "LEFT JOIN users u1 ON m.sender_id = u1.user_id " +
                     "LEFT JOIN users u2 ON m.receiver_id = u2.user_id " +
                     "LEFT JOIN listings l ON m.listing_id = l.listing_id " +
                     "WHERE m.sender_id = ? " +
                     "ORDER BY m.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    public Message getMessageById(int messageId) {
        String sql = "SELECT m.*, u1.name as sender_name, u2.name as receiver_name, l.title as listing_title " +
                     "FROM messages m " +
                     "LEFT JOIN users u1 ON m.sender_id = u1.user_id " +
                     "LEFT JOIN users u2 ON m.receiver_id = u2.user_id " +
                     "LEFT JOIN listings l ON m.listing_id = l.listing_id " +
                     "WHERE m.message_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, messageId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean markAsRead(int messageId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE message_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        int listingId = rs.getInt("listing_id");
        if (!rs.wasNull()) {
            message.setListingId(listingId);
        }
        message.setSubject(rs.getString("subject"));
        message.setContent(rs.getString("content"));
        message.setRead(rs.getBoolean("is_read"));
        message.setCreatedAt(rs.getTimestamp("created_at"));
        message.setSenderName(rs.getString("sender_name"));
        message.setReceiverName(rs.getString("receiver_name"));
        message.setListingTitle(rs.getString("listing_title"));
        return message;
    }
}





