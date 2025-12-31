<%@ include file="common/header.jsp" %>
<%@ page import="com.bookexchange.model.User" %>
<%@ page import="com.bookexchange.dao.UserDAO" %>

<h2>Compose Message</h2>

<%
    String receiverIdStr = (String) request.getAttribute("receiverId");
    String listingIdStr = (String) request.getAttribute("listingId");
    
    int receiverId = 0;
    String receiverName = "Unknown User";
    
    if (receiverIdStr != null && !receiverIdStr.isEmpty()) {
        receiverId = Integer.parseInt(receiverIdStr);
        UserDAO userDAO = new UserDAO();
        User receiver = userDAO.getUserById(receiverId);
        if (receiver != null) {
            receiverName = receiver.getName();
        }
    }
%>

<div class="row justify-content-center">
    <div class="col-md-8">
        <div class="card">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/messages/send" method="post">
                    <input type="hidden" name="receiverId" value="<%= receiverId %>">
                    <% if (listingIdStr != null && !listingIdStr.isEmpty()) { %>
                        <input type="hidden" name="listingId" value="<%= listingIdStr %>">
                    <% } %>
                    
                    <div class="mb-3">
                        <label class="form-label"><strong>To:</strong></label>
                        <p class="form-control-plaintext"><%= receiverName %></p>
                    </div>
                    
                    <div class="mb-3">
                        <label for="subject" class="form-label">Subject</label>
                        <input type="text" class="form-control" id="subject" name="subject" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="content" class="form-label">Message</label>
                        <textarea class="form-control" id="content" name="content" rows="6" required></textarea>
                    </div>
                    
                    <button type="submit" class="btn btn-primary">Send Message</button>
                    <a href="${pageContext.request.contextPath}/messages/inbox" class="btn btn-secondary">Cancel</a>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="common/footer.jsp" %>