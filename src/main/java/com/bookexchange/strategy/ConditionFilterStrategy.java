package com.bookexchange.strategy;

import com.bookexchange.model.Listing;
import java.util.ArrayList;
import java.util.List;

public class ConditionFilterStrategy implements FilterStrategy {
    @Override
    public List<Listing> filter(List<Listing> listings, String filterValue) {
        List<Listing> results = new ArrayList<>();
        for (Listing listing : listings) {
            if (listing.getConditionType() != null && 
                listing.getConditionType().equalsIgnoreCase(filterValue)) {
                results.add(listing);
            }
        }
        return results;
    }
}





