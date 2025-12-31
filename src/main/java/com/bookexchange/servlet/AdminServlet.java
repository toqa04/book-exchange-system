package com.bookexchange.servlet;

import com.bookexchange.dao.ListingDAO;
import com.bookexchange.dao.UserDAO;
import com.bookexchange.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private ListingDAO listingDAO = new ListingDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (!user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin privileges required.");
            return;
        }
        
        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            action = "/dashboard";
        }
        
        try {
            switch (action) {
                case "/dashboard":
                    showDashboard(request, response);
                    break;
                case "/users":
                    showUsers(request, response);
                    break;
                case "/listings":
                    showListings(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
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
        
        User user = (User) session.getAttribute("user");
        if (!user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin privileges required.");
            return;
        }
        
        String action = request.getPathInfo();
        
        try {
            switch (action) {
                case "/block-user":
                    blockUser(request, response);
                    break;
                case "/unblock-user":
                    unblockUser(request, response);
                    break;
                case "/delete-listing":
                    deleteListing(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "An error occurred: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        }
    }
    
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<User> allUsers = userDAO.getAllUsers();
            List<com.bookexchange.model.Listing> allListings = listingDAO.getAllActiveListings();
            
            request.setAttribute("totalUsers", allUsers != null ? allUsers.size() : 0);
            request.setAttribute("totalListings", allListings != null ? allListings.size() : 0);
            
            request.getRequestDispatcher("/jsp/admin-dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load dashboard: " + e.getMessage());
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void showUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<User> users = userDAO.getAllUsers();
            request.setAttribute("users", users);
            request.getRequestDispatcher("/jsp/admin-users.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load users: " + e.getMessage());
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void showListings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            listingDAO.expireListings();
            List<com.bookexchange.model.Listing> listings = listingDAO.getAllActiveListings();
            request.setAttribute("listings", listings);
            request.getRequestDispatcher("/jsp/admin-listings.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load listings: " + e.getMessage());
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
        }
    }
    
    private void blockUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            if (userDAO.blockUser(userId, true)) {
                request.getSession().setAttribute("success", "User blocked successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to block user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "Error blocking user: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
    
    private void unblockUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            if (userDAO.blockUser(userId, false)) {
                request.getSession().setAttribute("success", "User unblocked successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to unblock user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "Error unblocking user: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
    
    private void deleteListing(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int listingId = Integer.parseInt(request.getParameter("listingId"));
            if (listingDAO.deleteListing(listingId)) {
                request.getSession().setAttribute("success", "Listing deleted successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to delete listing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "Error deleting listing: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/listings");
    }
}