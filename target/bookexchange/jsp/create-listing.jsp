<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<h2>Create New Listing</h2>

<div class="card">
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/listings/create" method="post" enctype="multipart/form-data">
            <div class="mb-3">
                <label for="listingType" class="form-label">Listing Type</label>
                <select class="form-select" id="listingType" name="listingType" required onchange="togglePriceField()">
                    <option value="SELL">Sell</option>
                    <option value="EXCHANGE">Exchange</option>
                </select>
            </div>
            <div class="mb-3">
                <label for="title" class="form-label">Book Title *</label>
                <input type="text" class="form-control" id="title" name="title" required>
            </div>
            <div class="mb-3">
                <label for="author" class="form-label">Author</label>
                <input type="text" class="form-control" id="author" name="author">
            </div>
            <div class="mb-3">
                <label for="edition" class="form-label">Edition</label>
                <input type="text" class="form-control" id="edition" name="edition">
            </div>
            <div class="mb-3">
                <label for="courseCode" class="form-label">Course Code</label>
                <input type="text" class="form-control" id="courseCode" name="courseCode">
            </div>
            <div class="mb-3">
                <label for="categoryId" class="form-label">Category</label>
                <select class="form-select" id="categoryId" name="categoryId" required>
                    <option value="">Select Category</option>
                    <%
                        List<Map<String, Object>> categories = (List<Map<String, Object>>) request.getAttribute("categories");
                        if (categories != null) {
                            for (Map<String, Object> category : categories) {
                    %>
                        <option value="<%= category.get("categoryId") %>"><%= category.get("name") %></option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>
            <div class="mb-3">
                <label for="conditionType" class="form-label">Condition *</label>
                <select class="form-select" id="conditionType" name="conditionType" required>
                    <option value="NEW">New</option>
                    <option value="LIKE_NEW">Like New</option>
                    <option value="USED">Used</option>
                    <option value="DAMAGED">Damaged</option>
                </select>
            </div>
            <div class="mb-3" id="priceField">
                <label for="price" class="form-label">Price ($) *</label>
                <input type="number" step="0.01" class="form-control" id="price" name="price" min="0">
            </div>
            <div class="mb-3">
                <label for="image" class="form-label">Book Image (Max 5MB)</label>
                <input type="file" class="form-control" id="image" name="image" accept="image/*">
            </div>
            <button type="submit" class="btn btn-primary">Create Listing</button>
            <a href="${pageContext.request.contextPath}/listings" class="btn btn-secondary">Cancel</a>
        </form>
    </div>
</div>

<script>
function togglePriceField() {
    const listingType = document.getElementById('listingType').value;
    const priceField = document.getElementById('priceField');
    const priceInput = document.getElementById('price');
    
    if (listingType === 'SELL') {
        priceField.style.display = 'block';
        priceInput.required = true;
    } else {
        priceField.style.display = 'none';
        priceInput.required = false;
        priceInput.value = '';
    }
}

// Initialize on page load
togglePriceField();
</script>
<%@ include file="common/footer.jsp" %>





