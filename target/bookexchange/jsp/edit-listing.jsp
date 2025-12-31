<%@ include file="common/header.jsp" %>
<%@ page import="com.bookexchange.model.Listing" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
    Listing listing = (Listing) request.getAttribute("listing");
    if (listing == null) {
        response.sendRedirect(request.getContextPath() + "/listings/my-listings");
        return;
    }
%>

<h2>Edit Listing</h2>

<div class="card">
    <div class="card-body">
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
        <% } %>
        
        <form action="${pageContext.request.contextPath}/listings/update" method="post" enctype="multipart/form-data">
            <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
            
            <div class="mb-3">
                <label for="title" class="form-label">Book Title *</label>
                <input type="text" class="form-control" id="title" name="title" 
                       value="<%= listing.getTitle() %>" required>
            </div>
            <div class="mb-3">
                <label for="author" class="form-label">Author</label>
                <input type="text" class="form-control" id="author" name="author" 
                       value="<%= listing.getAuthor() != null ? listing.getAuthor() : "" %>">
            </div>
            <div class="mb-3">
                <label for="edition" class="form-label">Edition</label>
                <input type="text" class="form-control" id="edition" name="edition" 
                       value="<%= listing.getEdition() != null ? listing.getEdition() : "" %>">
            </div>
            <div class="mb-3">
                <label for="courseCode" class="form-label">Course Code</label>
                <input type="text" class="form-control" id="courseCode" name="courseCode" 
                       value="<%= listing.getCourseCode() != null ? listing.getCourseCode() : "" %>">
            </div>
            <div class="mb-3">
                <label for="categoryId" class="form-label">Category</label>
                <select class="form-select" id="categoryId" name="categoryId" required>
                    <%
                        List<Map<String, Object>> categories = (List<Map<String, Object>>) request.getAttribute("categories");
                        if (categories != null) {
                            for (Map<String, Object> category : categories) {
                    %>
                        <option value="<%= category.get("categoryId") %>" 
                                <%= listing.getCategoryId() == (Integer)category.get("categoryId") ? "selected" : "" %>>
                            <%= category.get("name") %>
                        </option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>
            <div class="mb-3">
                <label for="conditionType" class="form-label">Condition *</label>
                <select class="form-select" id="conditionType" name="conditionType" required>
                    <option value="NEW" <%= "NEW".equals(listing.getConditionType()) ? "selected" : "" %>>New</option>
                    <option value="LIKE_NEW" <%= "LIKE_NEW".equals(listing.getConditionType()) ? "selected" : "" %>>Like New</option>
                    <option value="USED" <%= "USED".equals(listing.getConditionType()) ? "selected" : "" %>>Used</option>
                    <option value="DAMAGED" <%= "DAMAGED".equals(listing.getConditionType()) ? "selected" : "" %>>Damaged</option>
                </select>
            </div>
            <% if (listing.isSellListing()) { %>
                <div class="mb-3">
                    <label for="price" class="form-label">Price ($) *</label>
                    <input type="number" step="0.01" class="form-control" id="price" name="price" 
                           value="<%= listing.getPrice() != null ? listing.getPrice() : "" %>" min="0" required>
                </div>
            <% } %>
            <div class="mb-3">
                <label for="image" class="form-label">Book Image (Max 5MB) - Leave empty to keep current</label>
                <input type="file" class="form-control" id="image" name="image" accept="image/*">
                <% if (listing.getImagePath() != null) { %>
                    <small class="form-text text-muted">Current: <%= listing.getImagePath() %></small>
                <% } %>
            </div>
            <button type="submit" class="btn btn-primary">Update Listing</button>
            <a href="${pageContext.request.contextPath}/listings/my-listings" class="btn btn-secondary">Cancel</a>
        </form>
    </div>
</div>
<%@ include file="common/footer.jsp" %>





