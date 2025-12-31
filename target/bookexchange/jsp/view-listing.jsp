<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jsp" %>
<%@ page import="com.bookexchange.model.Listing" %>
<%@ page import="com.bookexchange.model.User" %>
<%@ page import="com.bookexchange.dao.ReservationDAO" %>
<%@ page import="com.bookexchange.dao.ExchangeProposalDAO" %>
<%@ page import="com.bookexchange.dao.ListingDAO" %>
<%@ page import="com.bookexchange.model.Reservation" %>
<%@ page import="com.bookexchange.model.ExchangeProposal" %>
<%@ page import="java.util.List" %>

<%
    Listing listing = (Listing) request.getAttribute("listing");
    User currentUser = (User) session.getAttribute("user");
    if (listing == null) {
        response.sendRedirect(request.getContextPath() + "/listings");
        return;
    }
    
    ReservationDAO reservationDAO = new ReservationDAO();
    ExchangeProposalDAO proposalDAO = new ExchangeProposalDAO();
    ListingDAO listingDAO = new ListingDAO();
    Reservation reservation = null;
    List<ExchangeProposal> proposals = null;
    ExchangeProposal acceptedProposal = null;
    boolean isOwnerOfThisListing = false;
    boolean isProposerOfThisListing = false;
    Listing otherListing = null;
    
    // Get reservation info for sell listings
    if (listing.isSellListing() && "RESERVED".equals(listing.getStatus())) {
        reservation = reservationDAO.getActiveReservationByListing(listing.getListingId());
    }
    
    // Get proposals for exchange listings owned by current user (if AVAILABLE)
    if (listing.isExchangeListing() && currentUser != null && 
        listing.getUserId() == currentUser.getUserId() && "AVAILABLE".equals(listing.getStatus())) {
        proposals = proposalDAO.getProposalsForListing(listing.getListingId());
    }
    
    // Check if this is a RESERVED exchange listing and find the accepted proposal
    if (listing.isExchangeListing() && "RESERVED".equals(listing.getStatus()) && currentUser != null) {
        // Get all proposals for this listing (where this is the target listing)
        List<ExchangeProposal> allProposalsForListing = proposalDAO.getProposalsForListing(listing.getListingId());
        
        for (ExchangeProposal p : allProposalsForListing) {
            if ("ACCEPTED".equals(p.getStatus())) {
                // This listing is the TARGET listing (listing_id)
                // So the current user is the OWNER if they own this listing
                if (listing.getUserId() == currentUser.getUserId()) {
                    acceptedProposal = p;
                    isOwnerOfThisListing = true;
                    isProposerOfThisListing = false;
                    otherListing = listingDAO.getListingById(p.getProposedListingId());
                    break;
                }
            }
        }
        
        // If not found as owner, check if user is the proposer
        if (acceptedProposal == null) {
            List<ExchangeProposal> userProposals = proposalDAO.getUserProposals(currentUser.getUserId());
            for (ExchangeProposal p : userProposals) {
                if ("ACCEPTED".equals(p.getStatus())) {
                    // Check if THIS listing is the PROPOSED listing
                    if (p.getProposedListingId() == listing.getListingId()) {
                        acceptedProposal = p;
                        isOwnerOfThisListing = false;
                        isProposerOfThisListing = true;
                        otherListing = listingDAO.getListingById(p.getListingId());
                        break;
                    }
                }
            }
        }
    }
%>

<div class="row">
    <div class="col-md-6">
        <% if (listing.getImagePath() != null) { %>
            <img src="${pageContext.request.contextPath}/<%= listing.getImagePath() %>" 
                 class="img-fluid rounded" alt="<%= listing.getTitle() %>">
        <% } else { %>
            <div class="bg-secondary d-flex align-items-center justify-content-center rounded" style="height: 400px;">
                <span class="text-white">No Image Available</span>
            </div>
        <% } %>
    </div>
    <div class="col-md-6">
        <h2><%= listing.getTitle() %></h2>
        <hr>
        <p><strong>Author:</strong> <%= listing.getAuthor() != null ? listing.getAuthor() : "N/A" %></p>
        <p><strong>Edition:</strong> <%= listing.getEdition() != null ? listing.getEdition() : "N/A" %></p>
        <p><strong>Course Code:</strong> <%= listing.getCourseCode() != null ? listing.getCourseCode() : "N/A" %></p>
        <p><strong>Category:</strong> <%= listing.getCategoryName() != null ? listing.getCategoryName() : "N/A" %></p>
        <p><strong>Condition:</strong> <%= listing.getConditionType() %></p>
        <p><strong>Status:</strong> 
            <span class="badge bg-<%= "AVAILABLE".equals(listing.getStatus()) ? "success" : 
                                      "RESERVED".equals(listing.getStatus()) ? "warning" :
                                      "SOLD".equals(listing.getStatus()) ? "secondary" :
                                      "EXCHANGED".equals(listing.getStatus()) ? "info" : "dark" %>">
                <%= listing.getStatus() %>
            </span>
        </p>
        <p><strong>Listed by:</strong> <%= listing.getUserName() %></p>
        
        <% if (listing.isSellListing() && listing.getPrice() != null) { %>
            <h4 class="text-primary">$<%= listing.getPrice() %></h4>
        <% } %>
        
        <%-- Show exchange confirmation status if applicable --%>
        <% if (acceptedProposal != null) { %>
            <div class="alert alert-warning mb-3">
                <h5>Exchange Confirmation Status</h5>
                <% if (otherListing != null) { %>
                    <p class="mb-2 text-info">
                        <strong>Exchanging:</strong><br>
                        Your book: <strong><%= listing.getTitle() %></strong><br>
                        Their book: <strong><%= otherListing.getTitle() %></strong>
                    </p>
                <% } %>
                <p class="mb-2">
                    Owner Confirmed: <strong><%= acceptedProposal.isOwnerConfirmed() ? "✓ YES" : "✗ NO" %></strong><br>
                    Proposer Confirmed: <strong><%= acceptedProposal.isProposerConfirmed() ? "✓ YES" : "✗ NO" %></strong>
                </p>
                <% if (!acceptedProposal.isBothConfirmed()) { %>
                    <p class="text-muted small mb-0">Both parties must confirm to complete the exchange.</p>
                <% } else { %>
                    <p class="text-success small mb-0"><strong>✓ Both parties have confirmed! Exchange will be marked as complete.</strong></p>
                <% } %>
            </div>
        <% } %>
        
        <%-- NON-OWNER ACTIONS (User viewing someone else's listing) --%>
        <% if (currentUser != null && currentUser.getUserId() != listing.getUserId()) { %>
            <% if ("AVAILABLE".equals(listing.getStatus())) { %>
                <% if (listing.isSellListing()) { %>
                    <form action="<%= request.getContextPath() %>/transaction/reserve" method="post" class="d-inline">
                        <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
                        <button type="submit" class="btn btn-success">Reserve This Book</button>
                    </form>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jsp/select-exchange.jsp?targetListingId=<%= listing.getListingId() %>" 
                       class="btn btn-success">Propose Exchange</a>
                <% } %>
                <a href="${pageContext.request.contextPath}/messages/compose?receiverId=<%= listing.getUserId() %>&listingId=<%= listing.getListingId() %>" 
                   class="btn btn-primary">Message Seller</a>
                   
            <% } else if ("RESERVED".equals(listing.getStatus())) { %>
                <% if (listing.isSellListing() && reservation != null && reservation.getBuyerId() == currentUser.getUserId()) { %>
                    <div class="alert alert-info">
                        <strong>You have reserved this listing.</strong><br>
                        Please wait for the seller to confirm the transaction.
                    </div>
                    
                <% } else if (listing.isExchangeListing() && acceptedProposal != null && isProposerOfThisListing) { %>
                    <%-- This user is the PROPOSER, and they're viewing their PROPOSED listing --%>
                    <% 
                        boolean proposerConfirmed = acceptedProposal.isProposerConfirmed();
                    %>
                    <% if (!proposerConfirmed) { %>
                        <form action="${pageContext.request.contextPath}/transaction/confirm-exchange" method="post" class="d-inline mb-2">
                            <input type="hidden" name="proposalId" value="<%= acceptedProposal.getProposalId() %>">
                            <button type="submit" class="btn btn-success btn-lg" onclick="return confirm('Are you sure you want to confirm this exchange?');">
                                <i class="bi bi-check-circle"></i> Confirm Exchange
                            </button>
                        </form>
                        <div class="alert alert-info mt-2">
                            The listing owner has accepted your exchange proposal. Please confirm to complete the transaction.
                        </div>
                    <% } else { %>
                        <div class="alert alert-success">
                            <strong>✓ You have confirmed the exchange.</strong><br>
                            Waiting for the listing owner to confirm.
                        </div>
                    <% } %>
                <% } else { %>
                    <div class="alert alert-secondary">
                        This listing is currently reserved.
                    </div>
                <% } %>
                
                <a href="${pageContext.request.contextPath}/messages/compose?receiverId=<%= listing.getUserId() %>&listingId=<%= listing.getListingId() %>" 
                   class="btn btn-primary">Message Seller</a>
                   
            <% } else { %>
                <div class="alert alert-secondary">
                    This listing is no longer available.
                </div>
            <% } %>
        <% } %>
        
        <%-- OWNER ACTIONS (User viewing their own listing) --%>
        <% if (currentUser != null && currentUser.getUserId() == listing.getUserId()) { %>
            <% if ("AVAILABLE".equals(listing.getStatus())) { %>
                <%-- Show exchange proposals if any --%>
                <% if (listing.isExchangeListing() && proposals != null && !proposals.isEmpty()) { %>
                    <div class="mt-3">
                        <h5>Exchange Proposals (<%= proposals.size() %>)</h5>
                        <% for (ExchangeProposal proposal : proposals) { %>
                            <div class="card mb-3 border-primary">
                                <div class="card-body">
                                    <h6 class="card-title">New Proposal</h6>
                                    <p class="mb-2"><strong>Proposed by:</strong> <%= proposal.getProposerName() %></p>
                                    <% 
                                        Listing proposedListing = listingDAO.getListingById(proposal.getProposedListingId());
                                        if (proposedListing != null) {
                                    %>
                                        <div class="alert alert-light mb-3">
                                            <p class="mb-1"><strong>They're offering:</strong></p>
                                            <p class="mb-0">
                                                <strong><%= proposedListing.getTitle() %></strong>
                                                <% if (proposedListing.getCourseCode() != null) { %>
                                                    - <%= proposedListing.getCourseCode() %>
                                                <% } %>
                                                <br>
                                                <small class="text-muted">
                                                    Condition: <%= proposedListing.getConditionType() %>
                                                </small>
                                            </p>
                                            <a href="${pageContext.request.contextPath}/listings/view?id=<%= proposedListing.getListingId() %>" 
                                               class="btn btn-sm btn-outline-primary mt-2" target="_blank">
                                                View Their Book
                                            </a>
                                        </div>
                                    <% } %>
                                    <div class="btn-group" role="group">
                                        <form action="${pageContext.request.contextPath}/transaction/accept-exchange" method="post" class="d-inline">
                                            <input type="hidden" name="proposalId" value="<%= proposal.getProposalId() %>">
                                            <button type="submit" class="btn btn-success" onclick="return confirm('Accept this exchange proposal? Both listings will be reserved and you will need to confirm to complete the exchange.');">
                                                <i class="bi bi-check-lg"></i> Accept Proposal
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/transaction/reject-exchange" method="post" class="d-inline">
                                            <input type="hidden" name="proposalId" value="<%= proposal.getProposalId() %>">
                                            <button type="submit" class="btn btn-danger ms-2" onclick="return confirm('Reject this proposal?');">
                                                <i class="bi bi-x-lg"></i> Reject
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <div class="alert alert-info">
                        This is your listing. 
                        <% if (listing.isExchangeListing()) { %>
                            Waiting for exchange proposals.
                        <% } else { %>
                            Waiting for buyers.
                        <% } %>
                    </div>
                <% } %>
                
            <% } else if ("RESERVED".equals(listing.getStatus())) { %>
                <div class="alert alert-warning">
                    <strong><i class="bi bi-lock-fill"></i> This listing is reserved.</strong>
                </div>
                
                <% if (listing.isSellListing() && reservation != null) { %>
                    <%-- SELL LISTING ACTIONS --%>
                    <p><strong>Reserved by:</strong> <%= reservation.getBuyerName() %></p>
                    <div class="d-grid gap-2">
                        <form action="${pageContext.request.contextPath}/transaction/confirm-sale" method="post">
                            <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
                            <button type="submit" class="btn btn-success btn-lg w-100" onclick="return confirm('Mark this listing as SOLD?');">
                                <i class="bi bi-check-circle"></i> Mark as Sold
                            </button>
                        </form>
                        <form action="${pageContext.request.contextPath}/transaction/cancel-reservation" method="post">
                            <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
                            <button type="submit" class="btn btn-outline-warning w-100" onclick="return confirm('Cancel this reservation? The listing will become available again.');">
                                <i class="bi bi-x-circle"></i> Cancel Reservation
                            </button>
                        </form>
                    </div>
                    
                <% } else if (listing.isExchangeListing() && acceptedProposal != null && isOwnerOfThisListing) { %>
                    <%-- EXCHANGE LISTING ACTIONS - This user is the OWNER --%>
                    <% 
                        boolean ownerConfirmed = acceptedProposal.isOwnerConfirmed();
                    %>
                    
                    <% if (!ownerConfirmed) { %>
                        <div class="d-grid gap-2">
                            <form action="${pageContext.request.contextPath}/transaction/confirm-exchange" method="post">
                                <input type="hidden" name="proposalId" value="<%= acceptedProposal.getProposalId() %>">
                                <button type="submit" class="btn btn-success btn-lg w-100" onclick="return confirm('Are you sure you want to confirm this exchange?');">
                                    <i class="bi bi-check-circle-fill"></i> Confirm Exchange
                                </button>
                            </form>
                        </div>
                        <div class="alert alert-info mt-3">
                            <i class="bi bi-info-circle"></i> You have accepted this exchange proposal. Please confirm to complete the transaction.
                        </div>
                    <% } else { %>
                        <div class="alert alert-success">
                            <i class="bi bi-check-circle-fill"></i> <strong>You have confirmed the exchange.</strong><br>
                            Waiting for the proposer to confirm.
                        </div>
                    <% } %>
                <% } %>
                
            <% } else { %>
                <div class="alert alert-secondary">
                    <strong>Status:</strong> <%= listing.getStatus() %><br>
                    This listing is no longer active.
                </div>
            <% } %>
        <% } %>
    </div>
</div>

<%@ include file="common/footer.jsp" %>