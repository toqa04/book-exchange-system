package com.bookexchange.model;

import java.sql.Timestamp;

public class Reservation {
    private int reservationId;
    private int listingId;
    private int buyerId;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display
    private Listing listing;
    private String buyerName;
    
    public Reservation() {}
    
    // Getters and Setters
    public int getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
    
    public int getListingId() {
        return listingId;
    }
    
    public void setListingId(int listingId) {
        this.listingId = listingId;
    }
    
    public int getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Listing getListing() {
        return listing;
    }
    
    public void setListing(Listing listing) {
        this.listing = listing;
    }
    
    public String getBuyerName() {
        return buyerName;
    }
    
    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }
    
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }
}

