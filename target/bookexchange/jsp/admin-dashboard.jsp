<%@ include file="common/header.jsp" %>

<h2>Admin Dashboard</h2>

<div class="row">
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Total Users</h5>
                <h2><%= request.getAttribute("totalUsers") %></h2>
                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-primary">Manage Users</a>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Active Listings</h5>
                <h2><%= request.getAttribute("totalListings") %></h2>
                <a href="${pageContext.request.contextPath}/admin/listings" class="btn btn-primary">Manage Listings</a>
            </div>
        </div>
    </div>
</div>
<%@ include file="common/footer.jsp" %>





