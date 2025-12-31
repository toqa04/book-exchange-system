    </div> <!-- closes the .container or whatever wrapper you have above -->

    <footer class="bg-light text-center py-3 mt-5 border-top">
        <div class="container">
            <p class="mb-0 text-muted">&copy; 2025 Book Exchange Platform. All rights reserved.</p>
        </div>
    </footer>

    <!-- Bootstrap 5 Bundle (already here - kept) -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Real-time Updates Script (THIS IS THE KEY LINE) -->
    <script src="${pageContext.request.contextPath}/js/realtime-updates.js"></script>

    <!-- Toast Container (required for beautiful toasts) -->
    <div id="toastContainer" class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 9999;"></div>

</body>
</html>