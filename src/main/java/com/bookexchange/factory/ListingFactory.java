package com.bookexchange.factory;

import com.bookexchange.model.Listing;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;

/**
 * Factory Method Pattern for creating different types of listings
 */
public class ListingFactory {
    
    public static Listing createSellListing(int userId, String title, String author, String edition,
                                           String courseCode, int categoryId, String conditionType,
                                           String imagePath, BigDecimal price) {
        Listing listing = new Listing();
        listing.setUserId(userId);
        listing.setListingType("SELL");
        listing.setTitle(title);
        listing.setAuthor(author);
        listing.setEdition(edition);
        listing.setCourseCode(courseCode);
        listing.setCategoryId(categoryId);
        listing.setConditionType(conditionType);
        listing.setImagePath(imagePath);
        listing.setPrice(price);
        listing.setStatus("AVAILABLE");
        
        // Set expiry date to 30 days from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        listing.setExpiryDate(new Date(cal.getTimeInMillis()));
        
        return listing;
    }
    
    public static Listing createExchangeListing(int userId, String title, String author, String edition,
                                               String courseCode, int categoryId, String conditionType,
                                               String imagePath) {
        Listing listing = new Listing();
        listing.setUserId(userId);
        listing.setListingType("EXCHANGE");
        listing.setTitle(title);
        listing.setAuthor(author);
        listing.setEdition(edition);
        listing.setCourseCode(courseCode);
        listing.setCategoryId(categoryId);
        listing.setConditionType(conditionType);
        listing.setImagePath(imagePath);
        listing.setPrice(null); // Exchange listings don't have price
        listing.setStatus("AVAILABLE");
        
        // Set expiry date to 30 days from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        listing.setExpiryDate(new Date(cal.getTimeInMillis()));
        
        return listing;
    }
}





