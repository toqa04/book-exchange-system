<%@ include file="common/header.jsp" %>
<%@ page import="com.bookexchange.model.Message" %>
<%@ page import="com.bookexchange.model.User" %>

<%
    Message message = (Message) request.getAttribute("message");
    User currentUser = (User) session.getAttribute("user");
    
    if (message == null) {
        response.sendRedirect(request.getContextPath() + "/messages/inbox");
        return;
    }
%>

<h2>Message Details</h2>

<div class="row justify-content-center">
    <div class="col-md-8">
        <div class="card">
            <div class="card-header">
                <h5><%= message.getSubject() != null ? message.getSubject() : "(No Subject)" %></h5>
            </div>
            <div class="card-body">
                <p><strong>From:</strong> <%= message.getSenderName() %></p>
                <p><strong>To:</strong> <%= message.getReceiverName() %></p>
                <% if (message.getListingTitle() != null) { %>
                    <p><strong>Regarding Listing:</strong> <%= message.getListingTitle() %></p>
                <% } %>
                <p><strong>Date:</strong> <%= message.getCreatedAt() %></p>
                <hr>
                <div class="message-content">
                    <%= message.getContent() %>
                </div>
            </div>
            <div class="card-footer">
                <a href="${pageContext.request.contextPath}/messages/compose?receiverId=<%= message.getSenderId() %>" 
                   class="btn btn-primary">Reply</a>
                <a href="${pageContext.request.contextPath}/messages/inbox" class="btn btn-secondary">Back to Inbox</a>
            </div>
        </div>
    </div>
</div>

<%@ include file="common/footer.jsp" %>