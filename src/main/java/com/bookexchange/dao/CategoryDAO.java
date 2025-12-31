package com.bookexchange.dao;

import com.bookexchange.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {
    
    public List<Map<String, Object>> getAllCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("categoryId", rs.getInt("category_id"));
                category.put("name", rs.getString("name"));
                category.put("description", rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
    public List<Map<String, Object>> getAllCourseCodes() {
        List<Map<String, Object>> courses = new ArrayList<>();
        String sql = "SELECT * FROM course_codes ORDER BY code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> course = new HashMap<>();
                course.put("courseId", rs.getInt("course_id"));
                course.put("code", rs.getString("code"));
                course.put("name", rs.getString("name"));
                course.put("department", rs.getString("department"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
}





