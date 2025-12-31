package com.bookexchange.servlet;

import com.bookexchange.dao.ListingDAO;
import com.bookexchange.dao.CategoryDAO;
import com.bookexchange.factory.ListingFactory;
import com.bookexchange.model.Listing;
import com.bookexchange.model.User;
import com.bookexchange.util.FileUploadUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/listings/*")
@MultipartConfig(maxFileSize = 5242880) // 5MB
public class ListingServlet extends HttpServlet {
    private ListingDAO listingDAO = new ListingDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            action = "/list";
        }
        
        try {
            switch (action) {
                case "/list":
                    listListings(request, response);
                    break;
                case "/create":
                    showCreateForm(request, response);
                    break;
                case "/view":
                    viewListing(request, response);
                    break;
                case "/edit":
                    showEditForm(request, response);
                    break;
                case "/my-listings":
                    myListings(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("ERROR in ListingServlet doGet: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            action = "/create";
        }
        
        try {
            switch (action) {
                case "/create":
                    createListing(request, response);
                    break;
                case "/update":
                    updateListing(request, response);
                    break;
                case "/delete":
                    deleteListing(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("ERROR in ListingServlet doPost: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("error", "An error occurred: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/listings/my-listings");
        }
    }
    
    private void listListings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            System.out.println("DEBUG: Starting listListings...");
            
            // Expire old listings
            System.out.println("DEBUG: Expiring listings...");
            listingDAO.expireListings();
            
            // Get all active listings
            System.out.println("DEBUG: Getting all active listings...");
            List<Listing> listings = listingDAO.getAllActiveListings();
            if (listings == null) {
                listings = new ArrayList<>();
            }
            System.out.println("DEBUG: Found " + listings.size() + " listings");
            
            // Get categories - with error handling
            System.out.println("DEBUG: Getting categories...");
            List<?> categories = null;
            try {
                categories = categoryDAO.getAllCategories();
                if (categories == null) {
                    categories = new ArrayList<>();
                }
                System.out.println("DEBUG: Found " + categories.size() + " categories");
            } catch (Exception e) {
                System.err.println("ERROR loading categories: " + e.getMessage());
                e.printStackTrace();
                categories = new ArrayList<>();
            }
            
            // Set attributes
            request.setAttribute("listings", listings);
            request.setAttribute("categories", categories);
            
            System.out.println("DEBUG: Forwarding to listings.jsp...");
            request.getRequestDispatcher("/jsp/listings.jsp").forward(request, response);
            System.out.println("DEBUG: listListings completed successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR in listListings: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Error loading listings", e);
        }
    }
    
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user.isAdmin()) {
            request.getSession().setAttribute("error", "Admins cannot create listings.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        
        try {
            request.setAttribute("categories", categoryDAO.getAllCategories());
            request.setAttribute("courseCodes", categoryDAO.getAllCourseCodes());
        } catch (Exception e) {
            System.err.println("ERROR loading form data: " + e.getMessage());
            request.setAttribute("categories", new ArrayList<>());
            request.setAttribute("courseCodes", new ArrayList<>());
        }
        
        request.getRequestDispatcher("/jsp/create-listing.jsp").forward(request, response);
    }
    
    private void viewListing(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int listingId = Integer.parseInt(request.getParameter("id"));
            Listing listing = listingDAO.getListingById(listingId);
            if (listing != null) {
                request.setAttribute("listing", listing);
                request.getRequestDispatcher("/jsp/view-listing.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid listing ID");
        }
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        
        if (user.isAdmin()) {
            request.getSession().setAttribute("error", "Admins cannot edit listings.");
            response.sendRedirect(request.getContextPath() + "/admin/listings");
            return;
        }
        
        try {
            int listingId = Integer.parseInt(request.getParameter("id"));
            Listing listing = listingDAO.getListingById(listingId);
            
            if (listing != null && listing.getUserId() == user.getUserId() && listing.isAvailable()) {
                request.setAttribute("listing", listing);
                request.setAttribute("categories", categoryDAO.getAllCategories());
                request.setAttribute("courseCodes", categoryDAO.getAllCourseCodes());
                request.getRequestDispatcher("/jsp/edit-listing.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/listings/my-listings");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/listings/my-listings");
        }
    }
    
    private void myListings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        
        if (user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        
        try {
            List<Listing> listings = listingDAO.getUserListings(user.getUserId());
            if (listings == null) {
                listings = new ArrayList<>();
            }
            request.setAttribute("listings", listings);
            request.getRequestDispatcher("/jsp/my-listings.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("ERROR loading user listings: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("listings", new ArrayList<>());
            request.getRequestDispatcher("/jsp/my-listings.jsp").forward(request, response);
        }
    }
    
    private void createListing(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        
        if (user.isAdmin()) {
            request.getSession().setAttribute("error", "Admins cannot create listings.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        
        String listingType = request.getParameter("listingType");
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String edition = request.getParameter("edition");
        String courseCode = request.getParameter("courseCode");
        int categoryId = Integer.parseInt(request.getParameter("categoryId"));
        String conditionType = request.getParameter("conditionType");
        
        Part filePart = request.getPart("image");
        String imagePath = null;
        try {
            String appPath = request.getServletContext().getRealPath("");
            imagePath = FileUploadUtil.saveFile(filePart, appPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Listing listing;
        if ("SELL".equalsIgnoreCase(listingType)) {
            BigDecimal price = new BigDecimal(request.getParameter("price"));
            listing = ListingFactory.createSellListing(user.getUserId(), title, author, edition,
                    courseCode, categoryId, conditionType, imagePath, price);
        } else {
            listing = ListingFactory.createExchangeListing(user.getUserId(), title, author, edition,
                    courseCode, categoryId, conditionType, imagePath);
        }
        
        if (listingDAO.createListing(listing)) {
            response.sendRedirect(request.getContextPath() + "/listings/my-listings");
        } else {
            request.setAttribute("error", "Failed to create listing");
            request.setAttribute("categories", categoryDAO.getAllCategories());
            request.setAttribute("courseCodes", categoryDAO.getAllCourseCodes());
            request.getRequestDispatcher("/jsp/create-listing.jsp").forward(request, response);
        }
    }
    
    private void updateListing(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        
        if (user.isAdmin()) {
            request.getSession().setAttribute("error", "Admins cannot update listings.");
            response.sendRedirect(request.getContextPath() + "/admin/listings");
            return;
        }
        
        int listingId = Integer.parseInt(request.getParameter("listingId"));
        Listing existingListing = listingDAO.getListingById(listingId);
        
        if (existingListing == null || existingListing.getUserId() != user.getUserId() || 
            !existingListing.isAvailable()) {
            response.sendRedirect(request.getContextPath() + "/listings/my-listings");
            return;
        }
        
        existingListing.setTitle(request.getParameter("title"));
        existingListing.setAuthor(request.getParameter("author"));
        existingListing.setEdition(request.getParameter("edition"));
        existingListing.setCourseCode(request.getParameter("courseCode"));
        existingListing.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
        existingListing.setConditionType(request.getParameter("conditionType"));
        
        Part filePart = request.getPart("image");
        if (filePart != null && filePart.getSize() > 0) {
            try {
                String appPath = request.getServletContext().getRealPath("");
                String newImagePath = FileUploadUtil.saveFile(filePart, appPath);
                if (newImagePath != null) {
                    FileUploadUtil.deleteFile(existingListing.getImagePath(), appPath);
                    existingListing.setImagePath(newImagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (existingListing.isSellListing()) {
            existingListing.setPrice(new BigDecimal(request.getParameter("price")));
        }
        
        if (listingDAO.updateListing(existingListing)) {
            response.sendRedirect(request.getContextPath() + "/listings/my-listings");
        } else {
            request.setAttribute("error", "Failed to update listing");
            request.setAttribute("listing", existingListing);
            request.setAttribute("categories", categoryDAO.getAllCategories());
            request.setAttribute("courseCodes", categoryDAO.getAllCourseCodes());
            request.getRequestDispatcher("/jsp/edit-listing.jsp").forward(request, response);
        }
    }
    
    private void deleteListing(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        int listingId = Integer.parseInt(request.getParameter("listingId"));
        Listing listing = listingDAO.getListingById(listingId);
        
        if (listing != null && (listing.getUserId() == user.getUserId() || user.isAdmin())) {
            String appPath = request.getServletContext().getRealPath("");
            FileUploadUtil.deleteFile(listing.getImagePath(), appPath);
            listingDAO.deleteListing(listingId);
        }
        
        if (user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/listings");
        } else {
            response.sendRedirect(request.getContextPath() + "/listings/my-listings");
        }
    }
}