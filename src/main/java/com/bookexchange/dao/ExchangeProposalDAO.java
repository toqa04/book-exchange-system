package com.bookexchange.dao;

import com.bookexchange.model.ExchangeProposal;
import com.bookexchange.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeProposalDAO {
    
    public boolean createProposal(ExchangeProposal proposal) {
        String sql = "INSERT INTO exchange_proposals (listing_id, proposed_listing_id, proposer_id, status) " +
                     "VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, proposal.getListingId());
            stmt.setInt(2, proposal.getProposedListingId());
            stmt.setInt(3, proposal.getProposerId());
            
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    proposal.setProposalId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public ExchangeProposal getProposalById(int proposalId) {
        String sql = "SELECT * FROM exchange_proposals WHERE proposal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, proposalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProposal(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get PENDING proposals for a listing (for showing to owner)
     */
    public List<ExchangeProposal> getProposalsForListing(int listingId) {
        List<ExchangeProposal> proposals = new ArrayList<>();
        String sql = "SELECT ep.*, u.name as proposer_name " +
                     "FROM exchange_proposals ep " +
                     "LEFT JOIN users u ON ep.proposer_id = u.user_id " +
                     "WHERE ep.listing_id = ? AND ep.status = 'PENDING' " +
                     "ORDER BY ep.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                proposals.add(mapResultSetToProposal(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return proposals;
    }
    
    /**
     * NEW: Get ACCEPTED proposal for a listing (for my-listings page)
     */
    public ExchangeProposal getAcceptedProposalForListing(int listingId) {
        String sql = "SELECT ep.*, u.name as proposer_name " +
                     "FROM exchange_proposals ep " +
                     "LEFT JOIN users u ON ep.proposer_id = u.user_id " +
                     "WHERE ep.listing_id = ? AND ep.status = 'ACCEPTED' " +
                     "LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProposal(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<ExchangeProposal> getUserProposals(int userId) {
        List<ExchangeProposal> proposals = new ArrayList<>();
        String sql = "SELECT ep.*, u.name as proposer_name " +
                     "FROM exchange_proposals ep " +
                     "LEFT JOIN users u ON ep.proposer_id = u.user_id " +
                     "WHERE ep.proposer_id = ? " +
                     "ORDER BY ep.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                proposals.add(mapResultSetToProposal(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return proposals;
    }
    
    public boolean updateProposalStatus(int proposalId, String status) {
        String sql = "UPDATE exchange_proposals SET status = ? WHERE proposal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, proposalId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasPendingProposal(int listingId, int proposedListingId) {
        String sql = "SELECT COUNT(*) as count FROM exchange_proposals " +
                     "WHERE listing_id = ? AND proposed_listing_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, listingId);
            stmt.setInt(2, proposedListingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean confirmExchange(int proposalId, boolean isOwner) {
        String column = isOwner ? "owner_confirmed" : "proposer_confirmed";
        String sql = "UPDATE exchange_proposals SET " + column + " = TRUE WHERE proposal_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, proposalId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean areBothConfirmed(int proposalId) {
        String sql = "SELECT owner_confirmed, proposer_confirmed FROM exchange_proposals WHERE proposal_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, proposalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean ownerConfirmed = rs.getBoolean("owner_confirmed");
                boolean proposerConfirmed = rs.getBoolean("proposer_confirmed");
                return ownerConfirmed && proposerConfirmed;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private ExchangeProposal mapResultSetToProposal(ResultSet rs) throws SQLException {
        ExchangeProposal p = new ExchangeProposal();
        p.setProposalId(rs.getInt("proposal_id"));
        p.setListingId(rs.getInt("listing_id"));
        p.setProposedListingId(rs.getInt("proposed_listing_id"));
        p.setProposerId(rs.getInt("proposer_id"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            p.setOwnerConfirmed(rs.getBoolean("owner_confirmed"));
            p.setProposerConfirmed(rs.getBoolean("proposer_confirmed"));
        } catch (SQLException e) {
            p.setOwnerConfirmed(false);
            p.setProposerConfirmed(false);
        }

        try {
            String proposerName = rs.getString("proposer_name");
            if (proposerName != null) {
                p.setProposerName(proposerName);
            }
        } catch (SQLException e) {
            // Column doesn't exist, skip it
        }

        return p;
    }
}