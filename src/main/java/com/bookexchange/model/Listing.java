package com.bookexchange.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Listing {
    private int listingId;
    private int userId;
    private String listingType; // SELL or EXCHANGE
    private String title;
    private String author;
    private String edition;
    private String courseCode;
    private int categoryId;
    private String conditionType;
    private String imagePath;
    private BigDecimal price;
    private String status;
    private Date expiryDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display
    private String userName;
    private String categoryName;
    
    public Listing() {}
    
    // Getters and Setters
    public int getListingId() {
        return listingId;
    }
    
    public void setListingId(int listingId) {
        this.listingId = listingId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getListingType() {
        return listingType;
    }
    
    public void setListingType(String listingType) {
        this.listingType = listingType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getEdition() {
        return edition;
    }
    
    public void setEdition(String edition) {
        this.edition = edition;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getConditionType() {
        return conditionType;
    }
    
    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
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
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public boolean isSellListing() {
        return "SELL".equalsIgnoreCase(listingType);
    }
    
    public boolean isExchangeListing() {
        return "EXCHANGE".equalsIgnoreCase(listingType);
    }
    
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }
}





