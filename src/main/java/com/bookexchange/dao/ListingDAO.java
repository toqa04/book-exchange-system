package com.bookexchange.dao;

import com.bookexchange.model.Listing;
import com.bookexchange.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListingDAO {
    
    public boolean createListing(Listing listing) {
        String sql = "INSERT INTO listings (user_id, listing_type, title, author, edition, course_code, " +
                     "category_id, condition_type, image_path, price, expiry_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, listing.getUserId());
            stmt.setString(2, listing.getListingType().toUpperCase());
            stmt.setString(3, listing.getTitle());
            stmt.setString(4, listing.getAuthor());
            stmt.setString(5, listing.getEdition());
            stmt.setString(6, listing.getCourseCode());
            stmt.setInt(7, listing.getCategoryId());
            stmt.setString(8, listing.getConditionType());
            stmt.setString(9, listing.getImagePath());
            if (listing.getPrice() != null) {
                stmt.setBigDecimal(10, listing.getPrice());
            } else {
                stmt.setNull(10, Types.DECIMAL);
            }
            stmt.setDate(11, listing.getExpiryDate());
            stmt.setString(12, "AVAILABLE");
            
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    listing.setListingId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return false;
    }
    
    public Listing getListingById(int listingId) {
        String sql = "SELECT l.*, u.name as user_name, c.name as category_name " +
                     "FROM listings l " +
                     "LEFT JOIN users u ON l.user_id = u.user_id " +
                     "LEFT JOIN categories c ON l.category_id = c.category_id " +
                     "WHERE l.listing_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToListing(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }
    
    public List<Listing> getAllActiveListings() {
        List<Listing> listings = new ArrayList<>();
        String sql = "SELECT l.*, u.name as user_name, c.name as category_name " +
                     "FROM listings l " +
                     "LEFT JOIN users u ON l.user_id = u.user_id " +
                     "LEFT JOIN categories c ON l.category_id = c.category_id " +
                     "WHERE l.status = 'AVAILABLE' AND l.expiry_date >= CURRENT_DATE " +
                     "ORDER BY l.created_at DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                listings.add(mapResultSetToListing(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBConnection.closeConnection(conn);
        }
        return listings;
    }
    
    public List<Listing> getUserListings(int userId) {
        List<Listing> listings = new ArrayList<>();
        String sql = "SELECT l.*, u.name as user_name, c.name as category_name " +
                     "FROM listings l " +
                     "LEFT JOIN users u ON l.user_id = u.user_id " +
                     "LEFT JOIN categories c ON l.category_id = c.category_id " +
                     "WHERE l.user_id = ? " +
                     "ORDER BY l.created_at DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                listings.add(mapResultSetToListing(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return listings;
    }
    
    public List<Listing> getAvailableExchangeListings(int userId) {
        List<Listing> listings = new ArrayList<>();
        String sql = "SELECT l.*, u.name as user_name, c.name as category_name " +
                     "FROM listings l " +
                     "LEFT JOIN users u ON l.user_id = u.user_id " +
                     "LEFT JOIN categories c ON l.category_id = c.category_id " +
                     "WHERE l.listing_type = 'EXCHANGE' AND l.status = 'AVAILABLE' " +
                     "AND l.user_id = ? AND l.expiry_date >= CURRENT_DATE " +
                     "ORDER BY l.created_at DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                listings.add(mapResultSetToListing(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return listings;
    }
    
    public boolean updateListing(Listing listing) {
        String sql = "UPDATE listings SET title = ?, author = ?, edition = ?, course_code = ?, " +
                     "category_id = ?, condition_type = ?, image_path = ?, price = ? " +
                     "WHERE listing_id = ? AND status = 'AVAILABLE'";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, listing.getTitle());
            stmt.setString(2, listing.getAuthor());
            stmt.setString(3, listing.getEdition());
            stmt.setString(4, listing.getCourseCode());
            stmt.setInt(5, listing.getCategoryId());
            stmt.setString(6, listing.getConditionType());
            stmt.setString(7, listing.getImagePath());
            if (listing.getPrice() != null) {
                stmt.setBigDecimal(8, listing.getPrice());
            } else {
                stmt.setNull(8, Types.DECIMAL);
            }
            stmt.setInt(9, listing.getListingId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    public boolean deleteListing(int listingId) {
        String sql = "DELETE FROM listings WHERE listing_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, listingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    public boolean updateListingStatus(int listingId, String status) {
        String sql = "UPDATE listings SET status = ? WHERE listing_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, status.toUpperCase());
            stmt.setInt(2, listingId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    /**
     * IMPORTANT: This method uses a connection passed in (for transactions)
     * The connection should NOT be closed here
     */
    public boolean updateListingStatusWithLock(Connection conn, int listingId, String status) throws SQLException {
        String sql = "UPDATE listings SET status = ? WHERE listing_id = ? AND status = 'AVAILABLE'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, status.toUpperCase());
        stmt.setInt(2, listingId);
        int affected = stmt.executeUpdate();
        stmt.close();
        return affected > 0;
    }
    
    public void expireListings() {
        String sql = "UPDATE listings SET status = 'EXPIRED' WHERE expiry_date < CURRENT_DATE AND status = 'AVAILABLE'";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private Listing mapResultSetToListing(ResultSet rs) throws SQLException {
        Listing listing = new Listing();
        listing.setListingId(rs.getInt("listing_id"));
        listing.setUserId(rs.getInt("user_id"));
        listing.setListingType(rs.getString("listing_type"));
        listing.setTitle(rs.getString("title"));
        listing.setAuthor(rs.getString("author"));
        listing.setEdition(rs.getString("edition"));
        listing.setCourseCode(rs.getString("course_code"));
        listing.setCategoryId(rs.getInt("category_id"));
        listing.setConditionType(rs.getString("condition_type"));
        listing.setImagePath(rs.getString("image_path"));
        BigDecimal price = rs.getBigDecimal("price");
        if (rs.wasNull()) {
            price = null;
        }
        listing.setPrice(price);
        listing.setStatus(rs.getString("status"));
        listing.setExpiryDate(rs.getDate("expiry_date"));
        listing.setCreatedAt(rs.getTimestamp("created_at"));
        listing.setUpdatedAt(rs.getTimestamp("updated_at"));
        listing.setUserName(rs.getString("user_name"));
        listing.setCategoryName(rs.getString("category_name"));
        return listing;
    }
}