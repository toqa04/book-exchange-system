package com.bookexchange.servlet;

import com.bookexchange.dao.ListingDAO;
import com.bookexchange.dao.MessageDAO;
import com.bookexchange.model.Listing;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/*")
public class APIServlet extends HttpServlet {
    private ListingDAO listingDAO = new ListingDAO();
    private MessageDAO messageDAO = new MessageDAO();
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
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        switch (action != null ? action : "") {
            case "/my-listings-status":
                getMyListingsStatus(response, user);
                break;
            case "/listing-status":
                getListingStatus(request, response);
                break;
            case "/messages/unread-count":
                getUnreadMessageCount(response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void getMyListingsStatus(HttpServletResponse response, User user) throws IOException {
        PrintWriter out = response.getWriter();
        
        List<Listing> listings = listingDAO.getUserListings(user.getUserId());
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Listing listing : listings) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", listing.getListingId());
            item.put("status", listing.getStatus());
            result.add(item);
        }
        
        out.print(gson.toJson(result));
        out.flush();
    }
    
    private void getListingStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        PrintWriter out = response.getWriter();
        
        try {
            int listingId = Integer.parseInt(request.getParameter("id"));
            String lastStatus = request.getParameter("lastStatus");
            
            Listing listing = listingDAO.getListingById(listingId);
            
            Map<String, Object> result = new HashMap<>();
            if (listing != null) {
                result.put("status", listing.getStatus());
                result.put("changed", lastStatus != null && !lastStatus.equals(listing.getStatus()));
            } else {
                result.put("status", null);
                result.put("changed", false);
            }
            
            out.print(gson.toJson(result));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        out.flush();
    }
    
    private void getUnreadMessageCount(HttpServletResponse response, User user) throws IOException {
        PrintWriter out = response.getWriter();
        
        int count = messageDAO.getUnreadCount(user.getUserId());
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        
        out.print(gson.toJson(result));
        out.flush();
    }
}