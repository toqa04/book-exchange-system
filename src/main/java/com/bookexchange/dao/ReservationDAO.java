package com.bookexchange.dao;

import com.bookexchange.model.Reservation;
import com.bookexchange.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    
    /**
     * FIXED: Delete any existing PENDING reservations before creating new one
     */
    public boolean createReservation(Reservation r, Connection conn) throws SQLException {
        // First, delete any existing PENDING reservations for this listing
        String deleteSql = "DELETE FROM reservations WHERE listing_id = ? AND status = 'PENDING'";
        try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            deletePs.setInt(1, r.getListingId());
            deletePs.executeUpdate();
        }
        
        // Now insert the new reservation
        String insertSql = "INSERT INTO reservations (listing_id, buyer_id, status) VALUES (?, ?, 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getListingId());
            ps.setInt(2, r.getBuyerId());
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    r.setReservationId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }
    
    public Reservation getActiveReservationByListing(int listingId) {
        String sql = "SELECT r.*, u.name as buyer_name " +
                     "FROM reservations r " +
                     "LEFT JOIN users u ON r.buyer_id = u.user_id " +
                     "WHERE r.listing_id = ? AND r.status = 'PENDING' " +
                     "ORDER BY r.created_at DESC LIMIT 1";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }
    
    public List<Reservation> getUserReservations(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, u.name as buyer_name " +
                     "FROM reservations r " +
                     "LEFT JOIN users u ON r.buyer_id = u.user_id " +
                     "WHERE r.buyer_id = ? " +
                     "ORDER BY r.created_at DESC";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return reservations;
    }
    
    public List<Reservation> getSellerReservations(int sellerId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, u.name as buyer_name " +
                     "FROM reservations r " +
                     "LEFT JOIN users u ON r.buyer_id = u.user_id " +
                     "LEFT JOIN listings l ON r.listing_id = l.listing_id " +
                     "WHERE l.user_id = ? " +
                     "ORDER BY r.created_at DESC";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return reservations;
    }
    
    public boolean updateReservationStatus(int reservationId, String status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, reservationId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getInt("reservation_id"));
        reservation.setListingId(rs.getInt("listing_id"));
        reservation.setBuyerId(rs.getInt("buyer_id"));
        reservation.setStatus(rs.getString("status"));
        reservation.setCreatedAt(rs.getTimestamp("created_at"));
        reservation.setUpdatedAt(rs.getTimestamp("updated_at"));
        reservation.setBuyerName(rs.getString("buyer_name"));
        return reservation;
    }
    
    public boolean deleteReservationByListingId(int listingId) {
        String sql = "DELETE FROM reservations WHERE listing_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, listingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
}