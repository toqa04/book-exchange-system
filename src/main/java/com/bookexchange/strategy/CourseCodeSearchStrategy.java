package com.bookexchange.strategy;

import com.bookexchange.model.Listing;
import java.util.ArrayList;
import java.util.List;

public class CourseCodeSearchStrategy implements SearchStrategy {
    @Override
    public List<Listing> search(List<Listing> listings, String query) {
        List<Listing> results = new ArrayList<>();
        String upperQuery = query.toUpperCase();
        for (Listing listing : listings) {
            if (listing.getCourseCode() != null && listing.getCourseCode().toUpperCase().contains(upperQuery)) {
                results.add(listing);
            }
        }
        return results;
    }
}





