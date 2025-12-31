package com.bookexchange.strategy;

import com.bookexchange.model.Listing;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter listings by category ID
 */
public class CategoryFilterStrategy implements FilterStrategy {
    
    @Override
    public List<Listing> filter(List<Listing> listings, String criteria) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return listings;
        }
        
        List<Listing> result = new ArrayList<>();
        
        try {
            int categoryId = Integer.parseInt(criteria.trim());
            
            for (Listing listing : listings) {
                if (listing.getCategoryId() == categoryId) {
                    result.add(listing);
                }
            }
        } catch (NumberFormatException e) {
            // Invalid category ID, return empty list
            System.err.println("Invalid category ID format: " + criteria);
        }
        
        return result;
    }
}