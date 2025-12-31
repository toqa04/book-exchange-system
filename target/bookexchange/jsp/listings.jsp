<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bookexchange.model.Listing" %>

<h2>Browse Listings</h2>

<div class="card mb-4">
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/search" method="get">
            <div class="row g-2">
                <div class="col-md-3">
                    <label class="form-label small">Search By</label>
                    <select name="searchType" class="form-select">
                        <option value="TITLE" <%= "TITLE".equals(request.getAttribute("searchType")) ? "selected" : "" %>>Title</option>
                        <option value="COURSE" <%= "COURSE".equals(request.getAttribute("searchType")) ? "selected" : "" %>>Course Code</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label small">Search Query</label>
                    <input type="text" name="query" class="form-control" 
                           placeholder="Search..." value="<%= request.getAttribute("searchQuery") != null ? request.getAttribute("searchQuery") : "" %>">
                </div>
                
                <div class="col-md-2">
                    <label class="form-label small">Type</label>
                    <select name="type" class="form-select">
                        <option value="ALL">All Types</option>
                        <option value="SELL" <%= "SELL".equals(request.getAttribute("typeFilter")) ? "selected" : "" %>>Sell</option>
                        <option value="EXCHANGE" <%= "EXCHANGE".equals(request.getAttribute("typeFilter")) ? "selected" : "" %>>Exchange</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label small">Condition</label>
                    <select name="condition" class="form-select">
                        <option value="ALL">All Conditions</option>
                        <option value="NEW" <%= "NEW".equals(request.getAttribute("conditionFilter")) ? "selected" : "" %>>New</option>
                        <option value="LIKE_NEW" <%= "LIKE_NEW".equals(request.getAttribute("conditionFilter")) ? "selected" : "" %>>Like New</option>
                        <option value="USED" <%= "USED".equals(request.getAttribute("conditionFilter")) ? "selected" : "" %>>Used</option>
                        <option value="DAMAGED" <%= "DAMAGED".equals(request.getAttribute("conditionFilter")) ? "selected" : "" %>>Damaged</option>
                    </select>
                </div>
                
                <div class="col-12 col-md-6 col-lg-auto d-flex align-items-end mt-2">
                    <button type="submit" class="btn btn-primary w-100">Search</button>
                </div>
                
                <div class="col-12 col-md-6 col-lg-auto d-flex align-items-end mt-2">
                    <a href="${pageContext.request.contextPath}/listings/list" class="btn btn-outline-secondary w-100">Clear</a>
                </div>
            </div>
        </form>
    </div>
</div>

<%
    List<Listing> listings = (List<Listing>) request.getAttribute("listings");
    int totalResults = (listings != null) ? listings.size() : 0;
%>
<div class="mb-3">
    <p class="text-muted">Found <%= totalResults %> listing(s)</p>
</div>

<div class="row">
    <%
        if (listings != null && !listings.isEmpty()) {
            for (Listing listing : listings) {
                if (listing == null) continue;
    %>
    <div class="col-md-4 col-lg-3 mb-4">
        <div class="card h-100">
            <% if (listing.getImagePath() != null && !listing.getImagePath().trim().isEmpty()) { %>
                <img src="${pageContext.request.contextPath}/<%= listing.getImagePath() %>" 
                     class="card-img-top" alt="<%= listing.getTitle() != null ? listing.getTitle() : "Book" %>" 
                     style="height: 200px; object-fit: cover;">
            <% } else { %>
                <div class="card-img-top bg-secondary d-flex align-items-center justify-content-center" style="height: 200px;">
                    <span class="text-white">No Image</span>
                </div>
            <% } %>
            <div class="card-body">
                <h5 class="card-title"><%= listing.getTitle() != null ? listing.getTitle() : "Untitled" %></h5>
                <p class="card-text small">
                    <strong>Author:</strong> <%= listing.getAuthor() != null && !listing.getAuthor().trim().isEmpty() ? listing.getAuthor() : "N/A" %><br>
                    <strong>Course:</strong> <%= listing.getCourseCode() != null && !listing.getCourseCode().trim().isEmpty() ? listing.getCourseCode() : "N/A" %><br>
                    <strong>Condition:</strong> <span class="badge bg-secondary"><%= listing.getConditionType() != null ? listing.getConditionType() : "UNKNOWN" %></span><br>
                    <% if (listing.isSellListing() && listing.getPrice() != null) { %>
                        <strong>Price:</strong> <span class="text-primary fw-bold">$<%= listing.getPrice() %></span>
                    <% } else { %>
                        <strong>Type:</strong> <span class="badge bg-info">Exchange</span>
                    <% } %>
                </p>
            </div>
            <div class="card-footer">
                <a href="${pageContext.request.contextPath}/listings/view?id=<%= listing.getListingId() %>" 
                   class="btn btn-primary btn-sm w-100">View Details</a>
            </div>
        </div>
    </div>
    <%
            }
        } else {
    %>
    <div class="col-12">
        <div class="alert alert-info">
            No listings found matching your criteria. Try adjusting your filters.
        </div>
    </div>
    <%
        }
    %>
</div>

<%@ include file="common/footer.jsp" %>