<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bookexchange.model.Message" %>

<h2>Inbox</h2>

<%
    List<Message> messages = (List<Message>) request.getAttribute("messages");
    Object unreadCountObj = request.getAttribute("unreadCount");
%>

<% if (unreadCountObj != null && ((Integer) unreadCountObj) > 0) { %>
    <div class="alert alert-info">You have <%= unreadCountObj %> unread message(s)</div>
<% } %>

<div class="card">
    <div class="card-body">
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>From</th>
                    <th>Subject</th>
                    <th>Listing</th>
                    <th>Date</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (messages != null && !messages.isEmpty()) {
                        for (Message message : messages) {
                %>
                <tr class="<%= !message.isRead() ? "table-warning" : "" %>">
                    <td><%= message.getSenderName() %></td>
                    <td><%= message.getSubject() != null ? message.getSubject() : "(No Subject)" %></td>
                    <td><%= message.getListingTitle() != null ? message.getListingTitle() : "N/A" %></td>
                    <td><%= message.getCreatedAt() %></td>
                    <td>
                        <a href="${pageContext.request.contextPath}/messages/view?id=<%= message.getMessageId() %>" 
                           class="btn btn-sm btn-primary">View</a>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="5" class="text-center">No messages</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="common/footer.jsp" %>