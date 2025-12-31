<%@ include file="common/header.jsp" %>
<%@ page import="com.bookexchange.model.User" %>

<%
    User currentUser = (User) request.getAttribute("user");
    if (currentUser == null) {
        currentUser = (User) session.getAttribute("user");
    }
    
    String successMsg = (String) session.getAttribute("success");
    String errorMsg = (String) session.getAttribute("error");
    
    if (successMsg != null) {
        session.removeAttribute("success");
%>
    <div class="alert alert-success alert-dismissible fade show">
        <%= successMsg %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
<%
    }
    if (errorMsg != null) {
        session.removeAttribute("error");
%>
    <div class="alert alert-danger alert-dismissible fade show">
        <%= errorMsg %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
<%
    }
%>

<div class="row justify-content-center">
    <div class="col-md-8">
        <div class="card">
            <div class="card-header">
                <h3><i class="bi bi-person-circle"></i> My Profile</h3>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/profile" method="post" id="profileForm">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="name" class="form-label">Full Name *</label>
                            <input type="text" class="form-control" id="name" name="name" 
                                   value="<%= currentUser.getName() %>" required>
                        </div>
                        
                        <div class="col-md-6 mb-3">
                            <label for="email" class="form-label">Email Address *</label>
                            <input type="email" class="form-control" id="email" name="email" 
                                   value="<%= currentUser.getEmail() %>" required>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="year" class="form-label">Academic Year</label>
                            <select class="form-select" id="year" name="year">
                                <option value="">Not specified</option>
                                <option value="Freshman" <%= "Freshman".equals(currentUser.getYear()) ? "selected" : "" %>>Freshman</option>
                                <option value="Sophomore" <%= "Sophomore".equals(currentUser.getYear()) ? "selected" : "" %>>Sophomore</option>
                                <option value="Junior" <%= "Junior".equals(currentUser.getYear()) ? "selected" : "" %>>Junior</option>
                                <option value="Senior" <%= "Senior".equals(currentUser.getYear()) ? "selected" : "" %>>Senior</option>
                                <option value="Graduate" <%= "Graduate".equals(currentUser.getYear()) ? "selected" : "" %>>Graduate</option>
                            </select>
                        </div>
                        
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Role</label>
                            <input type="text" class="form-control" value="<%= currentUser.getRole() %>" disabled>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <label class="form-label">Account Status</label>
                        <input type="text" class="form-control" 
                               value="<%= currentUser.isActive() ? "Active" : "Blocked" %>" 
                               style="color: <%= currentUser.isActive() ? "green" : "red" %>;" disabled>
                    </div>
                    
                    <div class="mb-3">
                        <label class="form-label">Member Since</label>
                        <input type="text" class="form-control" 
                               value="<%= currentUser.getCreatedAt() != null ? currentUser.getCreatedAt().toString().substring(0, 10) : "N/A" %>" 
                               disabled>
                    </div>
                    
                    <hr>
                    
                    <div class="d-flex justify-content-between">
                        <div>
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check-circle"></i> Update Profile
                            </button>
                            <a href="${pageContext.request.contextPath}/<%= currentUser.isAdmin() ? "admin/dashboard" : "listings/my-listings" %>" 
                               class="btn btn-secondary">
                                <i class="bi bi-x-circle"></i> Cancel
                            </a>
                        </div>
                        <button type="button" class="btn btn-warning" onclick="showChangePasswordModal()">
                            <i class="bi bi-key"></i> Change Password
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Change Password Modal -->
<div class="modal fade" id="changePasswordModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Change Password</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/profile/change-password" method="post">
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="currentPassword" class="form-label">Current Password *</label>
                        <input type="password" class="form-control" id="currentPassword" 
                               name="currentPassword" required>
                    </div>
                    <div class="mb-3">
                        <label for="newPassword" class="form-label">New Password *</label>
                        <input type="password" class="form-control" id="newPassword" 
                               name="newPassword" required minlength="6">
                    </div>
                    <div class="mb-3">
                        <label for="confirmPassword" class="form-label">Confirm New Password *</label>
                        <input type="password" class="form-control" id="confirmPassword" 
                               name="confirmPassword" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Change Password</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function showChangePasswordModal() {
    var modal = new bootstrap.Modal(document.getElementById('changePasswordModal'));
    modal.show();
}

// Password confirmation validation
document.getElementById('confirmPassword').addEventListener('input', function() {
    var newPass = document.getElementById('newPassword').value;
    var confirmPass = this.value;
    if (newPass !== confirmPass) {
        this.setCustomValidity('Passwords do not match');
    } else {
        this.setCustomValidity('');
    }
});
</script>

<%@ include file="common/footer.jsp" %>