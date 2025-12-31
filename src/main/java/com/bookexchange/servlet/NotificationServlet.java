package com.bookexchange.servlet;

import com.bookexchange.dao.NotificationDAO;
import com.bookexchange.model.Notification;
import com.bookexchange.model.User;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/notifications/*")
public class NotificationServlet extends HttpServlet {
    private NotificationDAO notificationDAO = new NotificationDAO();
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        User user = (User) session.getAttribute("user");
        String action = request.getPathInfo();
        
        if (action == null || action.equals("/")) {
            action = "/list";
        }
        
        switch (action) {
            case "/list":
                listNotifications(request, response, user);
                break;
            case "/count":
                getUnreadCount(response, user);
                break;
            case "/recent":
                getRecentNotifications(response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        User user = (User) session.getAttribute("user");
        String action = request.getPathInfo();
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        switch (action) {
            case "/mark-read":
                markAsRead(request, response, user);
                break;
            case "/mark-all-read":
                markAllAsRead(response, user);
                break;
            case "/delete":
                deleteNotification(request, response, user);
                break;
            case "/delete-all":
                deleteAllNotifications(response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void listNotifications(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<Notification> notifications = notificationDAO.getUserNotifications(user.getUserId());
        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/jsp/notifications.jsp").forward(request, response);
    }
    
    private void getUnreadCount(HttpServletResponse response, User user) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        int count = notificationDAO.getUnreadCount(user.getUserId());
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        
        out.print(gson.toJson(result));
        out.flush();
    }
    
    private void getRecentNotifications(HttpServletResponse response, User user) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        List<Notification> notifications = notificationDAO.getUserNotifications(user.getUserId());
        
        out.print(gson.toJson(notifications));
        out.flush();
    }
    
    private void markAsRead(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            int notificationId = Integer.parseInt(request.getParameter("id"));
            boolean success = notificationDAO.markAsRead(notificationId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            out.print(gson.toJson(result));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        out.flush();
    }
    
    private void markAllAsRead(HttpServletResponse response, User user) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        boolean success = notificationDAO.markAllAsRead(user.getUserId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        
        out.print(gson.toJson(result));
        out.flush();
    }
    
    private void deleteNotification(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            int notificationId = Integer.parseInt(request.getParameter("id"));
            boolean success = notificationDAO.deleteNotification(notificationId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            out.print(gson.toJson(result));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        out.flush();
    }
    
    private void deleteAllNotifications(HttpServletResponse response, User user) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        boolean success = notificationDAO.deleteAllUserNotifications(user.getUserId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        
        out.print(gson.toJson(result));
        out.flush();
    }
}