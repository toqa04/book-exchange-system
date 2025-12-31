<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bookexchange.model.Message" %>

<h2>Outbox (Sent Messages)</h2>

<%
    List<Message> messages = (List<Message>) request.getAttribute("messages");
%>

<div class="card">
    <div class="card-body">
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>To</th>
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
                <tr>
                    <td><%= message.getReceiverName() %></td>
                    <td><%= message.getSubject() != null ? message.getSubject() : "(No Subject)" %></td>
                    <td><%= message.getListingTitle() != null ? message.getListingTitle() : "N/A" %></td>
                    <td><%= message.getCreatedAt() %></td>
                    <td>
                        <span class="text-muted">Sent</span>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="5" class="text-center">No sent messages</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>
</div>

<div class="mt-3">
    <a href="${pageContext.request.contextPath}/messages/inbox" class="btn btn-secondary">Back to Inbox</a>
</div>

<%@ include file="common/footer.jsp" %>