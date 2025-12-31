package com.bookexchange.servlet;

import com.bookexchange.dao.UserDAO;
import com.bookexchange.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet({"/profile", "/profile/change-password"})
public class ProfileServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        // Refresh user data from database
        User sessionUser = (User) session.getAttribute("user");
        User freshUser = userDAO.getUserById(sessionUser.getUserId());
        
        if (freshUser != null) {
            session.setAttribute("user", freshUser);
            request.setAttribute("user", freshUser);
        } else {
            request.setAttribute("user", sessionUser);
        }
        
        request.getRequestDispatcher("/jsp/profile.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        String path = request.getServletPath();
        
        if ("/profile/change-password".equals(path)) {
            changePassword(request, response);
        } else {
            updateProfile(request, response);
        }
    }
    
    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String year = request.getParameter("year");
        
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            session.setAttribute("error", "Name cannot be empty.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            session.setAttribute("error", "Valid email is required.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Check if email is already taken by another user
        if (!email.equals(user.getEmail()) && userDAO.emailExists(email)) {
            session.setAttribute("error", "Email is already in use by another account.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setYear(year);
        
        if (userDAO.updateUser(user)) {
            // Refresh user in session
            User updatedUser = userDAO.getUserById(user.getUserId());
            session.setAttribute("user", updatedUser);
            session.setAttribute("success", "Profile updated successfully!");
        } else {
            session.setAttribute("error", "Failed to update profile. Please try again.");
        }
        
        response.sendRedirect(request.getContextPath() + "/profile");
    }
    
    private void changePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validate inputs
        if (currentPassword == null || newPassword == null || confirmPassword == null) {
            session.setAttribute("error", "All password fields are required.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("error", "New passwords do not match.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (newPassword.length() < 6) {
            session.setAttribute("error", "Password must be at least 6 characters long.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Verify current password
        if (!user.getPassword().equals(currentPassword)) {
            session.setAttribute("error", "Current password is incorrect.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Update password
        if (userDAO.changePassword(user.getUserId(), newPassword)) {
            user.setPassword(newPassword);
            session.setAttribute("user", user);
            session.setAttribute("success", "Password changed successfully!");
        } else {
            session.setAttribute("error", "Failed to change password. Please try again.");
        }
        
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}