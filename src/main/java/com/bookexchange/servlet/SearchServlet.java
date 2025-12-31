package com.bookexchange.servlet;

import com.bookexchange.dao.ListingDAO;
import com.bookexchange.dao.CategoryDAO;
import com.bookexchange.model.Listing;
import com.bookexchange.strategy.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    private ListingDAO listingDAO = new ListingDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        // Expire old listings
        listingDAO.expireListings();
        
        // Get all active listings
        List<Listing> listings = listingDAO.getAllActiveListings();
        List<Listing> results = new ArrayList<>(listings);
        
        // Get search and filter parameters
        String searchType = request.getParameter("searchType");
        String searchQuery = request.getParameter("query");
        String typeFilter = request.getParameter("type");
        String conditionFilter = request.getParameter("condition");
        String departmentFilter = request.getParameter("department");
        String categoryFilter = request.getParameter("category");
        
        // Apply search strategy if query exists
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            SearchStrategy searchStrategy = getSearchStrategy(searchType);
            if (searchStrategy != null) {
                results = searchStrategy.search(results, searchQuery.trim());
                request.setAttribute("searchQuery", searchQuery);
                request.setAttribute("searchType", searchType);
            }
        }
        
        // Apply type filter (SELL/EXCHANGE)
        if (typeFilter != null && !typeFilter.isEmpty() && !typeFilter.equals("ALL")) {
            FilterStrategy filterStrategy = new TypeFilterStrategy();
            results = filterStrategy.filter(results, typeFilter);
            request.setAttribute("typeFilter", typeFilter);
        }
        
        // Apply condition filter
        if (conditionFilter != null && !conditionFilter.isEmpty() && !conditionFilter.equals("ALL")) {
            FilterStrategy filterStrategy = new ConditionFilterStrategy();
            results = filterStrategy.filter(results, conditionFilter);
            request.setAttribute("conditionFilter", conditionFilter);
        }
        
        // Apply department filter (course code prefix)
        if (departmentFilter != null && !departmentFilter.isEmpty() && !departmentFilter.equals("ALL")) {
            FilterStrategy filterStrategy = new DepartmentFilterStrategy();
            results = filterStrategy.filter(results, departmentFilter);
            request.setAttribute("departmentFilter", departmentFilter);
        }
        
        // Apply category filter
        if (categoryFilter != null && !categoryFilter.isEmpty() && !categoryFilter.equals("ALL")) {
            FilterStrategy filterStrategy = new CategoryFilterStrategy();
            results = filterStrategy.filter(results, categoryFilter);
            request.setAttribute("categoryFilter", categoryFilter);
        }
        
        // Set results and categories for the view
        request.setAttribute("listings", results);
        request.setAttribute("categories", categoryDAO.getAllCategories());
        request.setAttribute("courseCodes", categoryDAO.getAllCourseCodes());
        
        // Forward to listings page
        request.getRequestDispatcher("/jsp/listings.jsp").forward(request, response);
    }
    
    /**
     * Map search type to appropriate strategy
     */
    private SearchStrategy getSearchStrategy(String searchType) {
        if (searchType == null || searchType.isEmpty()) {
            return new TitleSearchStrategy(); // Default to title search
        }
        
        switch (searchType.toUpperCase()) {
            case "TITLE":
                return new TitleSearchStrategy();
            case "COURSE":
            case "COURSE_CODE":
                return new CourseCodeSearchStrategy();
            default:
                return new TitleSearchStrategy();
        }
    }
}