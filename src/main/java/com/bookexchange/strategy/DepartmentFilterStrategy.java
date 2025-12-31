package com.bookexchange.strategy;

import com.bookexchange.model.Listing;
import com.bookexchange.dao.CategoryDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Filter listings by department (through course codes)
 */
public class DepartmentFilterStrategy implements FilterStrategy {
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    @Override
    public List<Listing> filter(List<Listing> listings, String filterValue) {
        List<Listing> results = new ArrayList<>();
        String lowerFilter = filterValue.toLowerCase().trim();
        
        // Get all course codes with their departments
        List<Map<String, Object>> courseCodes = categoryDAO.getAllCourseCodes();
        List<String> matchingCourseCodes = new ArrayList<>();
        
        // Find all course codes that belong to the filtered department
        for (Map<String, Object> courseCode : courseCodes) {
            String department = (String) courseCode.get("department");
            if (department != null && department.toLowerCase().contains(lowerFilter)) {
                matchingCourseCodes.add((String) courseCode.get("code"));
            }
        }
        
        // Filter listings by matching course codes
        for (Listing listing : listings) {
            if (listing.getCourseCode() != null) {
                for (String code : matchingCourseCodes) {
                    if (listing.getCourseCode().equalsIgnoreCase(code)) {
                        results.add(listing);
                        break; // Don't add the same listing twice
                    }
                }
            }
        }
        
        return results;
    }
}