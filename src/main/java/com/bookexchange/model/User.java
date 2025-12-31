package com.bookexchange.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String academicYear;
    private String role;
    private boolean isBlocked;
    private Timestamp createdAt;
    
    public User() {}
    
    public User(String name, String email, String password, String academicYear) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.academicYear = academicYear;
        this.role = "STUDENT";
        this.isBlocked = false;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    // FIXED: Added getYear() and setYear() methods
    public String getYear() {
        return academicYear;
    }
    
    public void setYear(String year) {
        this.academicYear = year;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isBlocked() {
        return isBlocked;
    }
    
    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
    
    // FIXED: Added isActive() method (inverse of isBlocked)
    public boolean isActive() {
        return !isBlocked;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }
}