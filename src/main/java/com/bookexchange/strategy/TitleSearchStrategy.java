package com.bookexchange.strategy;

import com.bookexchange.model.Listing;
import java.util.ArrayList;
import java.util.List;

public class TitleSearchStrategy implements SearchStrategy {
    @Override
    public List<Listing> search(List<Listing> listings, String query) {
        List<Listing> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Listing listing : listings) {
            if (listing.getTitle() != null && listing.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(listing);
            }
        }
        return results;
    }
}





