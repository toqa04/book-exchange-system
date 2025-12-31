<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bookexchange.model.Listing" %>
<%@ page import="com.bookexchange.model.User" %>
<%@ page import="com.bookexchange.dao.ListingDAO" %>

<%
    int targetListingId = Integer.parseInt(request.getParameter("targetListingId"));
    ListingDAO listingDAO = new ListingDAO();
    User currentUser = (User) session.getAttribute("user");
    
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }
    
    List<Listing> myExchangeListings = listingDAO.getAvailableExchangeListings(currentUser.getUserId());
%>

<h2>Select Your Listing for Exchange</h2>

<p>Select one of your available exchange listings to propose for this exchange:</p>

<div class="row">
    <%
        if (myExchangeListings != null && !myExchangeListings.isEmpty()) {
            for (Listing listing : myExchangeListings) {
    %>
    <div class="col-md-4 mb-4">
        <div class="card h-100">
            <% if (listing.getImagePath() != null) { %>
                <img src="${pageContext.request.contextPath}/<%= listing.getImagePath() %>" 
                     class="card-img-top" alt="<%= listing.getTitle() %>" style="height: 200px; object-fit: cover;">
            <% } %>
            <div class="card-body">
                <h5 class="card-title"><%= listing.getTitle() %></h5>
                <p class="card-text">
                    <strong>Author:</strong> <%= listing.getAuthor() != null ? listing.getAuthor() : "N/A" %><br>
                    <strong>Course:</strong> <%= listing.getCourseCode() != null ? listing.getCourseCode() : "N/A" %><br>
                    <strong>Condition:</strong> <%= listing.getConditionType() %>
                </p>
            </div>
            <div class="card-footer">
                <form action="${pageContext.request.contextPath}/transaction/propose-exchange" method="post">
                    <input type="hidden" name="listingId" value="<%= targetListingId %>">
                    <input type="hidden" name="proposedListingId" value="<%= listing.getListingId() %>">
                    <button type="submit" class="btn btn-success w-100">Propose This Book</button>
                </form>
            </div>
        </div>
    </div>
    <%
            }
        } else {
    %>
    <div class="col-12">
        <div class="alert alert-warning">
            You don't have any available exchange listings. 
            <a href="${pageContext.request.contextPath}/listings/create">Create one first!</a>
        </div>
    </div>
    <%
        }
    %>
</div>

<a href="${pageContext.request.contextPath}/listings/view?id=<%= targetListingId %>" class="btn btn-secondary">Cancel</a>
<%@ include file="common/footer.jsp" %>

