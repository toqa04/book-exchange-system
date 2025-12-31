<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bookexchange.model.User" %>

<h2><i class="bi bi-people"></i> User Management</h2>

<%
    List<User> users = (List<User>) request.getAttribute("users");
%>

<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Year</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                    if (users != null && !users.isEmpty()) {
                        for (User u : users) {
                    %>
                    <tr>
                        <td><%= u.getUserId() %></td>
                        <td><%= u.getName() %></td>
                        <td><%= u.getEmail() %></td>
                        <td><%= u.getYear() != null ? u.getYear() : "N/A" %></td>
                        <td>
                            <% if (u.isAdmin()) { %>
                                <span class="badge bg-danger">Admin</span>
                            <% } else { %>
                                <span class="badge bg-primary">Student</span>
                            <% } %>
                        </td>
                        <td>
                            <% if (u.isBlocked()) { %>
                                <span class="badge bg-secondary">Blocked</span>
                            <% } else { %>
                                <span class="badge bg-success">Active</span>
                            <% } %>
                        </td>
                        <td><%= u.getCreatedAt() != null ? u.getCreatedAt().toString().substring(0, 10) : "N/A" %></td>
                        <td>
                            <% if (!u.isAdmin()) { %>
                                <% if (u.isBlocked()) { %>
                                    <form action="${pageContext.request.contextPath}/admin/unblock-user" method="post" style="display: inline;">
                                        <input type="hidden" name="userId" value="<%= u.getUserId() %>">
                                        <button type="submit" class="btn btn-sm btn-success" onclick="return confirm('Unblock this user?')">
                                            <i class="bi bi-unlock"></i> Unblock
                                        </button>
                                    </form>
                                <% } else { %>
                                    <form action="${pageContext.request.contextPath}/admin/block-user" method="post" style="display: inline;">
                                        <input type="hidden" name="userId" value="<%= u.getUserId() %>">
                                        <button type="submit" class="btn btn-sm btn-warning" onclick="return confirm('Block this user?')">
                                            <i class="bi bi-lock"></i> Block
                                        </button>
                                    </form>
                                <% } %>
                                <button type="button" class="btn btn-sm btn-info" 
                                        onclick="showMessageModal(<%= u.getUserId() %>, '<%= u.getName() %>')">
                                    <i class="bi bi-envelope"></i> Message
                                </button>
                            <% } else { %>
                                <span class="text-muted">Admin</span>
                            <% } %>
                        </td>
                    </tr>
                    <% 
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="8" class="text-center">No users found</td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Message Modal -->
<div class="modal fade" id="messageModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Send Message to <span id="recipientName"></span></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/messages/send" method="post">
                <input type="hidden" id="recipientId" name="receiverId">
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="subject" class="form-label">Subject</label>
                        <input type="text" class="form-control" id="subject" name="subject" required>
                    </div>
                    <div class="mb-3">
                        <label for="content" class="form-label">Message</label>
                        <textarea class="form-control" id="content" name="content" rows="5" required></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-send"></i> Send Message
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function showMessageModal(userId, userName) {
    document.getElementById('recipientId').value = userId;
    document.getElementById('recipientName').textContent = userName;
    var modal = new bootstrap.Modal(document.getElementById('messageModal'));
    modal.show();
}
</script>

<%@ include file="common/footer.jsp" %>