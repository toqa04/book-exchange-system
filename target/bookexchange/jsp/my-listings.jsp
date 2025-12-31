<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jsp" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bookexchange.model.Listing" %>
<%@ page import="com.bookexchange.model.ExchangeProposal" %>
<%@ page import="com.bookexchange.dao.ExchangeProposalDAO" %>
<%@ page import="com.bookexchange.dao.ReservationDAO" %>
<%@ page import="com.bookexchange.dao.ListingDAO" %>
<%@ page import="com.bookexchange.model.Reservation" %>
<%@ page import="com.bookexchange.model.User" %>

<h2>My Listings</h2>

<%
    Object successObj = session.getAttribute("success");
    Object errorObj = session.getAttribute("error");
    Object infoObj = session.getAttribute("info");
    
    if (successObj != null) {
%>
    <div class="alert alert-success alert-dismissible fade show">
        <%= successObj %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
<%
        session.removeAttribute("success");
    }
    if (errorObj != null) {
%>
    <div class="alert alert-danger alert-dismissible fade show">
        <%= errorObj %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
<%
        session.removeAttribute("error");
    }
    if (infoObj != null) {
%>
    <div class="alert alert-info alert-dismissible fade show">
        <%= infoObj %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
<%
        session.removeAttribute("info");
    }
%>

<%
    List<Listing> listings = (List<Listing>) request.getAttribute("listings");
    User currentUser = (User) session.getAttribute("user");
    
    ExchangeProposalDAO proposalDAO = null;
    ReservationDAO reservationDAO = null;
    ListingDAO listingDAO = null;
    
    try {
        proposalDAO = new ExchangeProposalDAO();
        reservationDAO = new ReservationDAO();
        listingDAO = new ListingDAO();
    } catch (Exception e) {
        out.println("<!-- Error initializing DAOs: " + e.getMessage() + " -->");
    }
    
    if (listings != null && !listings.isEmpty()) {
%>
<div class="row">
    <%
        for (Listing listing : listings) {
            if (listing == null) continue;
            
            ExchangeProposal acceptedProposal = null;
            boolean isOwnerOfThisListing = false;
            boolean isProposerOfThisListing = false;
            
            if (proposalDAO != null && "RESERVED".equals(listing.getStatus()) && listing.isExchangeListing() && currentUser != null) {
                try {
                    // FIXED LOGIC: Check BOTH possibilities
                    
                    // 1. Check if this listing is the TARGET (listing_id in proposal)
                    ExchangeProposal targetProposal = proposalDAO.getAcceptedProposalForListing(listing.getListingId());
                    if (targetProposal != null) {
                        // This listing IS the target, so current user is the OWNER
                        acceptedProposal = targetProposal;
                        isOwnerOfThisListing = true;
                        isProposerOfThisListing = false;
                    } else {
                        // 2. Check if this listing is the PROPOSED listing (proposed_listing_id in proposal)
                        List<ExchangeProposal> userProposals = proposalDAO.getUserProposals(currentUser.getUserId());
                        if (userProposals != null) {
                            for (ExchangeProposal p : userProposals) {
                                if (p != null && "ACCEPTED".equals(p.getStatus()) && p.getProposedListingId() == listing.getListingId()) {
                                    acceptedProposal = p;
                                    isOwnerOfThisListing = false;
                                    isProposerOfThisListing = true;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    out.println("<!-- Error loading proposals: " + e.getMessage() + " -->");
                    e.printStackTrace();
                }
            }
            
            Reservation reservation = null;
            if (reservationDAO != null && "RESERVED".equals(listing.getStatus()) && listing.isSellListing()) {
                try {
                    reservation = reservationDAO.getActiveReservationByListing(listing.getListingId());
                } catch (Exception e) {
                    out.println("<!-- Error loading reservation: " + e.getMessage() + " -->");
                }
            }
    %>
    <div class="col-md-6 col-lg-4 mb-4">
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
                    <strong>Type:</strong> 
                    <span class="badge bg-<%= listing.isSellListing() ? "success" : "info" %>">
                        <%= listing.getListingType() != null ? listing.getListingType() : "UNKNOWN" %>
                    </span><br>
                    <strong>Status:</strong> 
                    <span class="badge bg-<%= listing.getStatus() != null && listing.getStatus().equals("AVAILABLE") ? "success" : 
                                                 listing.getStatus() != null && listing.getStatus().equals("RESERVED") ? "warning text-dark" :
                                                 listing.getStatus() != null && listing.getStatus().equals("SOLD") ? "secondary" : 
                                                 listing.getStatus() != null && listing.getStatus().equals("EXCHANGED") ? "info" : "dark" %>">
                        <%= listing.getStatus() != null ? listing.getStatus() : "UNKNOWN" %>
                    </span><br>
                    <% if (listing.isSellListing() && listing.getPrice() != null) { %>
                        <strong>Price:</strong> $<%= listing.getPrice() %><br>
                    <% } %>
                    <strong>Course:</strong> <%= listing.getCourseCode() != null && !listing.getCourseCode().trim().isEmpty() ? listing.getCourseCode() : "N/A" %><br>
                    <strong>Condition:</strong> <%= listing.getConditionType() != null ? listing.getConditionType() : "UNKNOWN" %>
                </p>
                
                <%-- Show exchange confirmation status if applicable --%>
                <% if (acceptedProposal != null) { %>
                    <div class="alert alert-warning alert-sm mb-2 p-2">
                        <small>
                            <strong>Exchange Status:</strong><br>
                            Owner: <%= acceptedProposal.isOwnerConfirmed() ? "✓ Confirmed" : "⏳ Pending" %><br>
                            Proposer: <%= acceptedProposal.isProposerConfirmed() ? "✓ Confirmed" : "⏳ Pending" %>
                            <% if (isOwnerOfThisListing) { %>
                                <br><span class="badge bg-primary mt-1">You are the Owner</span>
                            <% } else if (isProposerOfThisListing) { %>
                                <br><span class="badge bg-info mt-1">You are the Proposer</span>
                            <% } %>
                        </small>
                    </div>
                    <% if (listingDAO != null && acceptedProposal != null) {
                        try {
                            Listing otherListing = null;
                            if (isOwnerOfThisListing) {
                                otherListing = listingDAO.getListingById(acceptedProposal.getProposedListingId());
                            } else if (isProposerOfThisListing) {
                                otherListing = listingDAO.getListingById(acceptedProposal.getListingId());
                            }
                            if (otherListing != null) {
                    %>
                        <div class="alert alert-info alert-sm mb-2 p-2">
                            <small><strong>Exchanging with:</strong> <%= otherListing.getTitle() %></small>
                        </div>
                    <%      }
                        } catch (Exception e) { }
                    } %>
                <% } %>
                
                <%-- Show reservation info for sell listings --%>
                <% if (reservation != null && reservation.getBuyerName() != null) { %>
                    <div class="alert alert-info alert-sm mb-2 p-2">
                        <small><strong>Reserved by:</strong> <%= reservation.getBuyerName() %></small>
                    </div>
                <% } %>
            </div>
            <div class="card-footer">
                <a href="${pageContext.request.contextPath}/listings/view?id=<%= listing.getListingId() %>" 
                   class="btn btn-primary btn-sm mb-1">View Details</a>
                   
                <% if (listing.isAvailable()) { %>
                    <a href="${pageContext.request.contextPath}/listings/edit?id=<%= listing.getListingId() %>" 
                       class="btn btn-warning btn-sm mb-1">Edit</a>
                    <form action="${pageContext.request.contextPath}/listings/delete" method="post" class="d-inline">
                        <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
                        <button type="submit" class="btn btn-danger btn-sm mb-1" 
                                onclick="return confirm('Are you sure you want to delete this listing?')">Delete</button>
                    </form>
                    
                <% } else if ("RESERVED".equals(listing.getStatus())) { %>
                    <% if (listing.isSellListing()) { %>
                        <%-- Sell listing actions --%>
                        <div class="d-grid gap-1">
                            <form action="${pageContext.request.contextPath}/transaction/confirm-sale" method="post">
                                <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
                                <button type="submit" class="btn btn-success btn-sm w-100" onclick="return confirm('Mark this as SOLD?');">
                                    <i class="bi bi-check-circle"></i> Mark Sold
                                </button>
                            </form>
                            <form action="${pageContext.request.contextPath}/transaction/cancel-reservation" method="post">
                                <input type="hidden" name="listingId" value="<%= listing.getListingId() %>">
                                <button type="submit" class="btn btn-outline-secondary btn-sm w-100" onclick="return confirm('Cancel reservation?');">
                                    <i class="bi bi-x-circle"></i> Cancel
                                </button>
                            </form>
                        </div>
                        
                    <% } else if (acceptedProposal != null) { %>
                        <%-- Exchange listing actions --%>
                        <% 
                            boolean userHasConfirmed = false;
                            if (isOwnerOfThisListing) {
                                userHasConfirmed = acceptedProposal.isOwnerConfirmed();
                            } else if (isProposerOfThisListing) {
                                userHasConfirmed = acceptedProposal.isProposerConfirmed();
                            }
                        %>
                        <div class="d-grid gap-1">
                            <% if (!userHasConfirmed) { %>
                                <form action="${pageContext.request.contextPath}/transaction/confirm-exchange" method="post" onsubmit="return confirm('Are you sure you want to confirm this exchange?');">
                                    <input type="hidden" name="proposalId" value="<%= acceptedProposal.getProposalId() %>">
                                    <button type="submit" class="btn btn-success btn-sm w-100">
                                        <i class="bi bi-check-circle"></i> Confirm Exchange
                                    </button>
                                </form>
                            <% } else { %>
                                <button class="btn btn-secondary btn-sm w-100" disabled title="Waiting for other party to confirm">
                                    <i class="bi bi-clock"></i> Confirmed
                                </button>
                            <% } %>
                            
                            <%-- Cancel Exchange button for both parties --%>
                            <form action="${pageContext.request.contextPath}/transaction/cancel-exchange" method="post" onsubmit="return confirm('Cancel this exchange? Both listings will become available again.');">
                                <input type="hidden" name="proposalId" value="<%= acceptedProposal.getProposalId() %>">
                                <button type="submit" class="btn btn-outline-danger btn-sm w-100">
                                    <i class="bi bi-x-lg"></i> Cancel Exchange
                                </button>
                            </form>
                        </div>
                    <% } %>
                <% } %>
            </div>
        </div>
    </div>
    <%
        }
    %>
</div>
<%
    } else {
%>
<div class="alert alert-info d-flex flex-wrap align-items-center justify-content-between gap-2">
    <div class="fw-semibold">You haven't created any listings yet.</div>
    <a href="${pageContext.request.contextPath}/listings/create" class="btn btn-sm btn-primary">Create one now</a>
</div>
<%
    }
%>

<%@ include file="common/footer.jsp" %>