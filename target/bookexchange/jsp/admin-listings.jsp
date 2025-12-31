<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bookexchange.model.Listing" %>

<h2>Listing Management</h2>

<div class="card">
    <div class="card-body">
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Owner</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<Listing> listings = (List<Listing>) request.getAttribute("listings");
                    if (listings != null) {
                        for (Listing listing : listings) {
                %>
                <tr>
                    <td><%= listing.getListingId() %></td>
                    <td><%= listing.getTitle() %></td>
                    <td><%= listing.getListingType() %></td>
                    <td><span class="badge bg-<%= listing.isAvailable() ? "success" : "warning" %>"><%= listing.getStatus() %></span></td>
                    <td><%= listing.getUserName() %></td>
                    <td>
                        <a href="${pageContext.request.contextPath}/listings/view?id=<%= listing.getListingId() %>" 
                           class="btn btn-sm btn-primary">View</a>
                        <form action="${pageContext.request.contextPath}/admin/delete-listing" method="post" class="d-inline">
                            <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
                            <button type="submit" class="btn btn-sm btn-danger" 
                                    onclick="return confirm('Are you sure you want to delete this listing?')">Delete</button>
                        </form>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="common/footer.jsp" %>





