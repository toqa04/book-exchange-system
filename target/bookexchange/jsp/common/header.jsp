<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.bookexchange.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    int unreadMessageCount = 0;
    int unreadNotificationCount = 0;
    
    if (user != null) {
        com.bookexchange.dao.MessageDAO messageDAO = new com.bookexchange.dao.MessageDAO();
        unreadMessageCount = messageDAO.getUnreadCount(user.getUserId());
        
        com.bookexchange.dao.NotificationDAO notificationDAO = new com.bookexchange.dao.NotificationDAO();
        unreadNotificationCount = notificationDAO.getUnreadCount(user.getUserId());
    }

    // Get and REMOVE messages so they show only once
    String success = (String) session.getAttribute("success");
    String error = (String) session.getAttribute("error");
    String info = (String) session.getAttribute("info");
    session.removeAttribute("success");
    session.removeAttribute("error");
    session.removeAttribute("info");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Book Exchange Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <style>
        .notification-bell {
            position: relative;
            cursor: pointer;
        }
        .notification-badge {
            position: absolute;
            top: -5px;
            right: -10px;
            background: #dc3545;
            color: white;
            border-radius: 50%;
            padding: 2px 6px;
            font-size: 11px;
            font-weight: bold;
        }
        .notification-dropdown {
            position: absolute;
            right: 0;
            top: 100%;
            width: 350px;
            max-height: 400px;
            overflow-y: auto;
            background: white;
            border: 1px solid #ddd;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: none;
            z-index: 1000;
            margin-top: 10px;
        }
        .notification-dropdown.show {
            display: block;
        }
        .notification-item {
            padding: 12px 16px;
            border-bottom: 1px solid #f0f0f0;
            cursor: pointer;
            transition: background 0.2s;
        }
        .notification-item:hover {
            background: #f8f9fa;
        }
        .notification-item.unread {
            background: #e7f3ff;
        }
        .notification-item .notification-title {
            font-weight: bold;
            color: #333;
            margin-bottom: 4px;
        }
        .notification-item .notification-message {
            color: #666;
            font-size: 14px;
            margin-bottom: 4px;
        }
        .notification-item .notification-time {
            color: #999;
            font-size: 12px;
        }
        .notification-header {
            padding: 12px 16px;
            border-bottom: 2px solid #0d6efd;
            font-weight: bold;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .notification-empty {
            padding: 40px 20px;
            text-align: center;
            color: #999;
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/listings">
                <i class="bi bi-book"></i> BookExchange
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/listings">
                            <i class="bi bi-grid"></i> Browse Listings
                        </a>
                    </li>
                    <% if (user != null && !user.isAdmin()) { %>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/listings/create">
                                <i class="bi bi-plus-circle"></i> Create Listing
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/listings/my-listings">
                                <i class="bi bi-bookmark"></i> My Listings
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/messages/inbox">
                                <i class="bi bi-envelope"></i> Messages
                                <% if (unreadMessageCount > 0) { %>
                                    <span class="badge bg-danger"><%= unreadMessageCount %></span>
                                <% } %>
                            </a>
                        </li>
                    <% } %>
                    <% if (user != null && user.isAdmin()) { %>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin/dashboard">
                                <i class="bi bi-speedometer2"></i> Admin Dashboard
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin/users">
                                <i class="bi bi-people"></i> Manage Users
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin/listings">
                                <i class="bi bi-list-check"></i> Manage Listings
                            </a>
                        </li>
                    <% } %>
                </ul>
                <ul class="navbar-nav">
                    <% if (user != null) { %>
                        <!-- NOTIFICATION BELL -->
                        <li class="nav-item" style="position: relative;">
                            <a class="nav-link notification-bell" id="notificationBell" href="javascript:void(0)">
                                <i class="bi bi-bell" style="font-size: 1.2rem;"></i>
                                <% if (unreadNotificationCount > 0) { %>
                                    <span class="notification-badge" id="notificationBadge"><%= unreadNotificationCount %></span>
                                <% } %>
                            </a>
                            <!-- Notification Dropdown -->
                            <div class="notification-dropdown" id="notificationDropdown">
                                <div class="notification-header">
                                    <span>Notifications</span>
                                    <button class="btn btn-sm btn-link text-primary" onclick="markAllRead()" style="text-decoration: none; padding: 0;">
                                        Mark all read
                                    </button>
                                </div>
                                <div id="notificationList">
                                    <div class="notification-empty">
                                        <i class="bi bi-bell-slash" style="font-size: 2rem;"></i>
                                        <p>No notifications</p>
                                    </div>
                                </div>
                            </div>
                        </li>
                        
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/profile">
                                <i class="bi bi-person-circle"></i> <%= user.getName() %>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/auth/logout">
                                <i class="bi bi-box-arrow-right"></i> Logout
                            </a>
                        </li>
                    <% } else { %>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/auth/login">Login</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/auth/register">Register</a>
                        </li>
                    <% } %>
                </ul>
            </div>
        </div>
    </nav>

    <!-- ALERTS -->
    <% if (success != null || error != null || info != null) { %>
        <div class="container mt-3">
            <% if (success != null) { %>
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="bi bi-check-circle"></i> <%= success %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            <% } %>
            <% if (error != null) { %>
                <div class="alert alert-danger alert-dismissible fade show">
                    <i class="bi bi-exclamation-triangle"></i> <%= error %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            <% } %>
            <% if (info != null) { %>
                <div class="alert alert-info alert-dismissible fade show">
                    <i class="bi bi-info-circle"></i> <%= info %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            <% } %>
        </div>
    <% } %>

    <div class="container mt-4">

<script>
// Notification System JavaScript
let notificationDropdownOpen = false;

// Toggle notification dropdown
document.addEventListener('DOMContentLoaded', function() {
    const bell = document.getElementById('notificationBell');
    const dropdown = document.getElementById('notificationDropdown');
    
    if (bell && dropdown) {
        bell.addEventListener('click', function(e) {
            e.stopPropagation();
            notificationDropdownOpen = !notificationDropdownOpen;
            
            if (notificationDropdownOpen) {
                dropdown.classList.add('show');
                loadNotifications();
            } else {
                dropdown.classList.remove('show');
            }
        });
        
        // Close dropdown when clicking outside
        document.addEventListener('click', function(e) {
            if (!dropdown.contains(e.target) && e.target !== bell) {
                dropdown.classList.remove('show');
                notificationDropdownOpen = false;
            }
        });
        
        // Load notifications on page load
        loadNotifications();
        
        // Poll for new notifications every 30 seconds
        setInterval(updateNotificationCount, 30000);
    }
});

// Load notifications via AJAX
function loadNotifications() {
    fetch('${pageContext.request.contextPath}/notifications/recent')
        .then(response => response.json())
        .then(notifications => {
            const listDiv = document.getElementById('notificationList');
            
            if (notifications.length === 0) {
                listDiv.innerHTML = '<div class="notification-empty">' +
                    '<i class="bi bi-bell-slash" style="font-size: 2rem;"></i>' +
                    '<p>No notifications</p>' +
                    '</div>';
            } else {
                listDiv.innerHTML = notifications.map(function(n) {
                    return '<div class="notification-item ' + (n.read ? '' : 'unread') + '" onclick="handleNotificationClick(' + n.notificationId + ', ' + n.relatedId + ')">' +
                        '<div class="notification-title">' + escapeHtml(n.title) + '</div>' +
                        '<div class="notification-message">' + escapeHtml(n.message) + '</div>' +
                        '<div class="notification-time">' + getTimeAgo(n.createdAt) + '</div>' +
                        '</div>';
                }).join('');
            }
        })
        .catch(error => console.error('Error loading notifications:', error));
}

// Update notification count badge
function updateNotificationCount() {
    fetch('${pageContext.request.contextPath}/notifications/count')
        .then(response => response.json())
        .then(data => {
            const badge = document.getElementById('notificationBadge');
            if (data.count > 0) {
                if (badge) {
                    badge.textContent = data.count;
                    badge.style.display = 'block';
                } else {
                    // Create badge if it doesn't exist
                    const bell = document.getElementById('notificationBell');
                    const newBadge = document.createElement('span');
                    newBadge.className = 'notification-badge';
                    newBadge.id = 'notificationBadge';
                    newBadge.textContent = data.count;
                    bell.appendChild(newBadge);
                }
            } else {
                if (badge) {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(error => console.error('Error updating count:', error));
}

// Mark all notifications as read
function markAllRead() {
    fetch('${pageContext.request.contextPath}/notifications/mark-all-read', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            loadNotifications();
            updateNotificationCount();
        }
    })
    .catch(error => console.error('Error marking all read:', error));
}

// Handle notification click
function handleNotificationClick(notificationId, relatedId) {
    // Mark as read
    fetch('${pageContext.request.contextPath}/notifications/mark-read', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'id=' + notificationId
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateNotificationCount();
            loadNotifications();
            
            // Navigate to related page if relatedId exists
            if (relatedId) {
                window.location.href = '${pageContext.request.contextPath}/listings/view?id=' + relatedId;
            }
        }
    })
    .catch(error => console.error('Error marking read:', error));
}

// Helper function to escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Helper function to format time ago
function getTimeAgo(timestamp) {
    const now = new Date();
    const then = new Date(timestamp);
    const diff = Math.floor((now - then) / 1000); // seconds
    
    if (diff < 60) return 'Just now';
    if (diff < 3600) return Math.floor(diff / 60) + ' minutes ago';
    if (diff < 86400) return Math.floor(diff / 3600) + ' hours ago';
    return Math.floor(diff / 86400) + ' days ago';
}
</script>