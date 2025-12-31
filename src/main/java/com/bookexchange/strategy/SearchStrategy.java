package com.bookexchange.strategy;

import com.bookexchange.model.Listing;
import java.util.List;

/**
 * Strategy Pattern interface for search and filter strategies
 */
public interface SearchStrategy {
    List<Listing> search(List<Listing> listings, String query);
}





