package com.bookexchange.strategy;

import com.bookexchange.model.Listing;
import java.util.List;

/**
 * Strategy Pattern interface for filtering listings
 */
public interface FilterStrategy {
    List<Listing> filter(List<Listing> listings, String filterValue);
}