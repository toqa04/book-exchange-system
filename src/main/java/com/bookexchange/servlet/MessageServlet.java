package com.bookexchange.servlet;

import com.bookexchange.dao.MessageDAO;
import com.bookexchange.model.Message;
import com.bookexchange.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/messages/*")
public class MessageServlet extends HttpServlet {
    private MessageDAO messageDAO = new MessageDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        String action = request.getPathInfo();
        
        if (action == null || action.equals("/")) {
            action = "/inbox";
        }
        
        switch (action) {
            case "/inbox":
                showInbox(request, response, user);
                break;
            case "/outbox":
                showOutbox(request, response, user);
                break;
            case "/view":
                viewMessage(request, response, user);
                break;
            case "/compose":
                showComposeForm(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
        String action = request.getPathInfo();
        
        if (action == null || action.equals("/")) {
            action = "/send";
        }
        
        switch (action) {
            case "/send":
                sendMessage(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void showInbox(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("messages", messageDAO.getInboxMessages(user.getUserId()));
        request.setAttribute("unreadCount", messageDAO.getUnreadCount(user.getUserId()));
        request.getRequestDispatcher("/jsp/inbox.jsp").forward(request, response);
    }
    
    private void showOutbox(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("messages", messageDAO.getOutboxMessages(user.getUserId()));
        request.getRequestDispatcher("/jsp/outbox.jsp").forward(request, response);
    }
    
    private void viewMessage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        int messageId = Integer.parseInt(request.getParameter("id"));
        Message message = messageDAO.getMessageById(messageId);
        
        if (message != null && message.getReceiverId() == user.getUserId()) {
            messageDAO.markAsRead(messageId);
            request.setAttribute("message", message);
            request.getRequestDispatcher("/jsp/view-message.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void showComposeForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String receiverId = request.getParameter("receiverId");
        String listingId = request.getParameter("listingId");
        request.setAttribute("receiverId", receiverId);
        request.setAttribute("listingId", listingId);
        request.getRequestDispatcher("/jsp/compose-message.jsp").forward(request, response);
    }
    
    private void sendMessage(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        int receiverId = Integer.parseInt(request.getParameter("receiverId"));
        String subject = request.getParameter("subject");
        String content = request.getParameter("content");
        
        Message message = new Message();
        message.setSenderId(user.getUserId());
        message.setReceiverId(receiverId);
        message.setSubject(subject);
        message.setContent(content);
        
        String listingIdStr = request.getParameter("listingId");
        if (listingIdStr != null && !listingIdStr.isEmpty()) {
            message.setListingId(Integer.parseInt(listingIdStr));
        }
        
        if (messageDAO.sendMessage(message)) {
            response.sendRedirect(request.getContextPath() + "/messages/outbox");
        } else {
            response.sendRedirect(request.getContextPath() + "/messages/compose?error=send_failed");
        }
    }
}





