package com.bookexchange.model;

import java.sql.Timestamp;

public class ExchangeProposal {
    private int proposalId;
    private int listingId;
    private int proposedListingId;
    private int proposerId;
    private String status;
    private Timestamp createdAt;
    private String proposerName;
    private boolean ownerConfirmed;
    private boolean proposerConfirmed;
    
    public ExchangeProposal() {
        this.ownerConfirmed = false;
        this.proposerConfirmed = false;
    }
    
    public ExchangeProposal(int listingId, int proposedListingId, int proposerId) {
        this.listingId = listingId;
        this.proposedListingId = proposedListingId;
        this.proposerId = proposerId;
        this.status = "PENDING";
        this.ownerConfirmed = false;
        this.proposerConfirmed = false;
    }
    
    public int getProposalId() {
        return proposalId;
    }
    
    public void setProposalId(int proposalId) {
        this.proposalId = proposalId;
    }
    
    public int getListingId() {
        return listingId;
    }
    
    public void setListingId(int listingId) {
        this.listingId = listingId;
    }
    
    public int getProposedListingId() {
        return proposedListingId;
    }
    
    public void setProposedListingId(int proposedListingId) {
        this.proposedListingId = proposedListingId;
    }
    
    public int getProposerId() {
        return proposerId;
    }
    
    public void setProposerId(int proposerId) {
        this.proposerId = proposerId;
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
    
    public String getProposerName() {
        return proposerName;
    }
    
    public void setProposerName(String proposerName) {
        this.proposerName = proposerName;
    }
    
    public boolean isOwnerConfirmed() {
        return ownerConfirmed;
    }
    
    public void setOwnerConfirmed(boolean ownerConfirmed) {
        this.ownerConfirmed = ownerConfirmed;
    }
    
    public boolean isProposerConfirmed() {
        return proposerConfirmed;
    }
    
    public void setProposerConfirmed(boolean proposerConfirmed) {
        this.proposerConfirmed = proposerConfirmed;
    }
    
    public boolean isBothConfirmed() {
        return ownerConfirmed && proposerConfirmed;
    }
    
    @Override
    public String toString() {
        return "ExchangeProposal{" +
                "proposalId=" + proposalId +
                ", listingId=" + listingId +
                ", proposedListingId=" + proposedListingId +
                ", proposerId=" + proposerId +
                ", status='" + status + '\'' +
                ", ownerConfirmed=" + ownerConfirmed +
                ", proposerConfirmed=" + proposerConfirmed +
                ", createdAt=" + createdAt +
                '}';
    }
}