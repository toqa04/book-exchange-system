package com.bookexchange.servlet;

import com.bookexchange.dao.*;
import com.bookexchange.model.*;
import com.bookexchange.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/transaction/*")
public class TransactionServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ExchangeProposalDAO proposalDAO = new ExchangeProposalDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String path = request.getPathInfo();

        try {
            switch (path != null ? path : "") {
                case "/reserve":
                    reserveListing(request, response, user);
                    break;
                case "/accept-exchange":
                    acceptExchange(request, response, user);
                    break;
                case "/confirm-sale":
                    confirmSale(request, response, user);
                    break;
                case "/cancel-reservation":
                    cancelReservation(request, response, user);
                    break;
                case "/propose-exchange":
                    proposeExchange(request, response, user);
                    break;
                case "/reject-exchange":
                    rejectExchange(request, response, user);
                    break;
                case "/confirm-exchange":
                    confirmExchange(request, response, user);
                    break;
                case "/cancel-proposal":
                    cancelProposal(request, response, user);
                    break;
                case "/cancel-exchange":
                    cancelExchange(request, response, user);
                    break;
                default:
                    response.sendError(404);
                    return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Something went wrong: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/listings/my-listings");
        }
    }

    private void reserveListing(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        
        if (user.isAdmin()) {
            req.getSession().setAttribute("error", "Admins cannot reserve listings.");
            resp.sendRedirect(req.getContextPath() + "/listings");
            return;
        }
        
        Connection conn = null;
        try {
            int id = Integer.parseInt(req.getParameter("listingId"));
            
            Listing listing = listingDAO.getListingById(id);
            if (listing == null) {
                req.getSession().setAttribute("error", "Listing not found.");
                resp.sendRedirect(req.getContextPath() + "/listings");
                return;
            }
            
            if (listing.getUserId() == user.getUserId()) {
                req.getSession().setAttribute("error", "You cannot reserve your own listing.");
                resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + id);
                return;
            }
            
            if (!listing.isAvailable()) {
                req.getSession().setAttribute("error", "This book is no longer available.");
                resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + id);
                return;
            }
            
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            if (listingDAO.updateListingStatusWithLock(conn, id, "RESERVED")) {
                Reservation r = new Reservation();
                r.setListingId(id);
                r.setBuyerId(user.getUserId());
                reservationDAO.createReservation(r, conn);
                
                Notification notification = new Notification(
                    listing.getUserId(),
                    "RESERVATION",
                    "Book Reserved",
                    user.getName() + " has reserved your book: " + listing.getTitle(),
                    id
                );
                notificationDAO.createNotification(notification);
                
                conn.commit();
                req.getSession().setAttribute("success", "Book reserved successfully!");
            } else {
                conn.rollback();
                req.getSession().setAttribute("error", "This book is no longer available.");
            }
            
            resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + id);
            
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to reserve listing: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/listings");
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private void acceptExchange(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        Connection conn = null;
        try {
            int pid = Integer.parseInt(req.getParameter("proposalId"));
            ExchangeProposal p = proposalDAO.getProposalById(pid);

            if (p == null) {
                req.getSession().setAttribute("error", "Invalid proposal.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            Listing target = listingDAO.getListingById(p.getListingId());
            Listing proposed = listingDAO.getListingById(p.getProposedListingId());
            
            if (target == null || target.getUserId() != user.getUserId()) {
                req.getSession().setAttribute("error", "You can only accept proposals for your own listings.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }
            
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            boolean listing1Updated = listingDAO.updateListingStatusWithLock(conn, p.getListingId(), "RESERVED");
            boolean listing2Updated = listingDAO.updateListingStatusWithLock(conn, p.getProposedListingId(), "RESERVED");
            
            if (listing1Updated && listing2Updated) {
                proposalDAO.updateProposalStatus(pid, "ACCEPTED");
                
                Notification notification = new Notification(
                    p.getProposerId(),
                    "EXCHANGE_ACCEPTED",
                    "Exchange Accepted!",
                    user.getName() + " accepted your exchange proposal for: " + target.getTitle(),
                    p.getListingId()
                );
                notificationDAO.createNotification(notification);
                
                conn.commit();
                req.getSession().setAttribute("success", "Exchange accepted! Both books are now reserved. Both parties must confirm to complete the exchange.");
            } else {
                conn.rollback();
                req.getSession().setAttribute("error", "One or both listings are no longer available.");
            }
            
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to accept exchange: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
        
        resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
    }

    private void confirmSale(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            int id = Integer.parseInt(req.getParameter("listingId"));
            Listing listing = listingDAO.getListingById(id);
            
            if (listing != null && listing.getUserId() == user.getUserId()) {
                if ("RESERVED".equals(listing.getStatus())) {
                    Reservation reservation = reservationDAO.getActiveReservationByListing(id);
                    
                    if (listingDAO.updateListingStatus(id, "SOLD")) {
                        if (reservation != null) {
                            Notification notification = new Notification(
                                reservation.getBuyerId(),
                                "TRANSACTION_COMPLETE",
                                "Purchase Complete",
                                "The seller has confirmed the sale of: " + listing.getTitle(),
                                id
                            );
                            notificationDAO.createNotification(notification);
                        }
                        
                        req.getSession().setAttribute("success", "Book marked as SOLD!");
                    } else {
                        req.getSession().setAttribute("error", "Failed to mark as sold.");
                    }
                } else {
                    req.getSession().setAttribute("error", "Only reserved listings can be marked as sold.");
                }
            } else {
                req.getSession().setAttribute("error", "You can only mark your own listings as sold.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to confirm sale: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
    }

    private void cancelReservation(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            int id = Integer.parseInt(req.getParameter("listingId"));
            Listing listing = listingDAO.getListingById(id);
            
            if (listing != null && listing.getUserId() == user.getUserId()) {
                if ("RESERVED".equals(listing.getStatus())) {
                    Reservation reservation = reservationDAO.getActiveReservationByListing(id);
                    
                    reservationDAO.deleteReservationByListingId(id);
                    if (listingDAO.updateListingStatus(id, "AVAILABLE")) {
                        if (reservation != null) {
                            Notification notification = new Notification(
                                reservation.getBuyerId(),
                                "RESERVATION",
                                "Reservation Cancelled",
                                "The seller cancelled your reservation for: " + listing.getTitle(),
                                id
                            );
                            notificationDAO.createNotification(notification);
                        }
                        
                        req.getSession().setAttribute("success", "Reservation cancelled!");
                    } else {
                        req.getSession().setAttribute("error", "Failed to cancel reservation.");
                    }
                } else {
                    req.getSession().setAttribute("error", "This listing is not reserved.");
                }
            } else {
                req.getSession().setAttribute("error", "You can only cancel reservations on your own listings.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to cancel reservation: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
    }

    private void proposeExchange(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        
        if (user.isAdmin()) {
            req.getSession().setAttribute("error", "Admins cannot propose exchanges.");
            resp.sendRedirect(req.getContextPath() + "/listings");
            return;
        }
        
        try {
            int targetId = Integer.parseInt(req.getParameter("listingId"));
            int myId = Integer.parseInt(req.getParameter("proposedListingId"));
            
            Listing targetListing = listingDAO.getListingById(targetId);
            Listing myListing = listingDAO.getListingById(myId);
            
            if (targetListing == null || myListing == null) {
                req.getSession().setAttribute("error", "Invalid listing(s).");
                resp.sendRedirect(req.getContextPath() + "/listings");
                return;
            }
            
            if (!targetListing.isAvailable() || !myListing.isAvailable()) {
                req.getSession().setAttribute("error", "One or both listings are not available for exchange.");
                resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + targetId);
                return;
            }
            
            if (myListing.getUserId() != user.getUserId()) {
                req.getSession().setAttribute("error", "You can only propose your own listings.");
                resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + targetId);
                return;
            }
            
            if (proposalDAO.hasPendingProposal(targetId, myId)) {
                req.getSession().setAttribute("error", "You already have a pending proposal for this exchange.");
                resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + targetId);
                return;
            }
            
            ExchangeProposal p = new ExchangeProposal();
            p.setListingId(targetId);
            p.setProposedListingId(myId);
            p.setProposerId(user.getUserId());
            
            if (proposalDAO.createProposal(p)) {
                Notification notification = new Notification(
                    targetListing.getUserId(),
                    "EXCHANGE_PROPOSAL",
                    "New Exchange Proposal",
                    user.getName() + " proposed to exchange their \"" + myListing.getTitle() + "\" for your \"" + targetListing.getTitle() + "\"",
                    targetId
                );
                notificationDAO.createNotification(notification);
                
                req.getSession().setAttribute("success", "Exchange proposed successfully!");
            } else {
                req.getSession().setAttribute("error", "Failed to create proposal.");
            }
            
            resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + targetId);
            
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to propose exchange: " + e.getMessage());
            int targetId = Integer.parseInt(req.getParameter("listingId"));
            resp.sendRedirect(req.getContextPath() + "/listings/view?id=" + targetId);
        }
    }

    private void rejectExchange(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            int pid = Integer.parseInt(req.getParameter("proposalId"));
            ExchangeProposal proposal = proposalDAO.getProposalById(pid);
            
            if (proposal != null) {
                Listing listing = listingDAO.getListingById(proposal.getListingId());
                if (listing != null && listing.getUserId() == user.getUserId()) {
                    if (proposalDAO.updateProposalStatus(pid, "REJECTED")) {
                        Notification notification = new Notification(
                            proposal.getProposerId(),
                            "EXCHANGE_REJECTED",
                            "Exchange Proposal Rejected",
                            user.getName() + " rejected your exchange proposal for: " + listing.getTitle(),
                            proposal.getListingId()
                        );
                        notificationDAO.createNotification(notification);
                        
                        req.getSession().setAttribute("info", "Proposal rejected.");
                    } else {
                        req.getSession().setAttribute("error", "Failed to reject proposal.");
                    }
                } else {
                    req.getSession().setAttribute("error", "You can only reject proposals for your own listings.");
                }
            } else {
                req.getSession().setAttribute("error", "Invalid proposal.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to reject proposal: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
    }

    private void confirmExchange(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            int pid = Integer.parseInt(req.getParameter("proposalId"));
            ExchangeProposal p = proposalDAO.getProposalById(pid);

            if (p == null) {
                req.getSession().setAttribute("error", "Proposal not found.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            if (!"ACCEPTED".equals(p.getStatus())) {
                req.getSession().setAttribute("error", "This exchange proposal is not in accepted status.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            Listing listing1 = listingDAO.getListingById(p.getListingId());
            Listing listing2 = listingDAO.getListingById(p.getProposedListingId());

            if (listing1 == null || listing2 == null) {
                req.getSession().setAttribute("error", "Invalid exchange listings.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            boolean isOwner = listing1.getUserId() == user.getUserId();
            boolean isProposer = listing2.getUserId() == user.getUserId();

            if (!isOwner && !isProposer) {
                req.getSession().setAttribute("error", "You are not part of this exchange.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            proposalDAO.confirmExchange(pid, isOwner);
            p = proposalDAO.getProposalById(pid);

            if (p.isBothConfirmed()) {
                boolean status1Updated = listingDAO.updateListingStatus(p.getListingId(), "EXCHANGED");
                boolean status2Updated = listingDAO.updateListingStatus(p.getProposedListingId(), "EXCHANGED");
                boolean proposalUpdated = proposalDAO.updateProposalStatus(pid, "COMPLETED");

                Notification notif1 = new Notification(
                    listing1.getUserId(),
                    "TRANSACTION_COMPLETE",
                    "Exchange Complete!",
                    "Your exchange for \"" + listing1.getTitle() + "\" is now complete!",
                    p.getListingId()
                );
                Notification notif2 = new Notification(
                    listing2.getUserId(),
                    "TRANSACTION_COMPLETE",
                    "Exchange Complete!",
                    "Your exchange for \"" + listing2.getTitle() + "\" is now complete!",
                    p.getProposedListingId()
                );
                notificationDAO.createNotification(notif1);
                notificationDAO.createNotification(notif2);

                req.getSession().setAttribute("success", "Exchange completed successfully! Both parties have confirmed.");
            } else {
                String otherParty = isOwner ? "proposer" : "owner";
                req.getSession().setAttribute("success", "You have confirmed the exchange. Waiting for the " + otherParty + " to confirm.");

                int otherUserId = isOwner ? listing2.getUserId() : listing1.getUserId();
                Notification notif = new Notification(
                    otherUserId,
                    "EXCHANGE_PROPOSAL",
                    "Confirmation Needed",
                    user.getName() + " has confirmed the exchange. Please confirm to complete the transaction.",
                    isOwner ? p.getListingId() : p.getProposedListingId()
                );
                notificationDAO.createNotification(notif);
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to confirm exchange: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
    }

    /**
     * NEW: Cancel a PENDING proposal (by proposer only)
     */
    private void cancelProposal(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            int pid = Integer.parseInt(req.getParameter("proposalId"));
            ExchangeProposal proposal = proposalDAO.getProposalById(pid);
            
            if (proposal == null) {
                req.getSession().setAttribute("error", "Proposal not found.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }
            
            // Only proposer can cancel their own proposal
            if (proposal.getProposerId() != user.getUserId()) {
                req.getSession().setAttribute("error", "You can only cancel your own proposals.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }
            
            // Can only cancel PENDING proposals
            if (!"PENDING".equals(proposal.getStatus())) {
                req.getSession().setAttribute("error", "You can only cancel pending proposals.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }
            
            if (proposalDAO.updateProposalStatus(pid, "CANCELLED")) {
                // Notify the listing owner
                Listing listing = listingDAO.getListingById(proposal.getListingId());
                if (listing != null) {
                    Notification notification = new Notification(
                        listing.getUserId(),
                        "EXCHANGE_PROPOSAL",
                        "Proposal Cancelled",
                        user.getName() + " cancelled their exchange proposal for: " + listing.getTitle(),
                        proposal.getListingId()
                    );
                    notificationDAO.createNotification(notification);
                }
                
                req.getSession().setAttribute("info", "Proposal cancelled successfully.");
            } else {
                req.getSession().setAttribute("error", "Failed to cancel proposal.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to cancel proposal: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
    }

    /**
     * NEW: Cancel an ACCEPTED exchange (by either party)
     * This reverts both listings back to AVAILABLE
     */
    private void cancelExchange(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        Connection conn = null;
        try {
            int pid = Integer.parseInt(req.getParameter("proposalId"));
            ExchangeProposal p = proposalDAO.getProposalById(pid);

            if (p == null) {
                req.getSession().setAttribute("error", "Proposal not found.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            if (!"ACCEPTED".equals(p.getStatus())) {
                req.getSession().setAttribute("error", "This exchange cannot be cancelled.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            Listing listing1 = listingDAO.getListingById(p.getListingId());
            Listing listing2 = listingDAO.getListingById(p.getProposedListingId());

            if (listing1 == null || listing2 == null) {
                req.getSession().setAttribute("error", "Invalid exchange listings.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            boolean isOwner = listing1.getUserId() == user.getUserId();
            boolean isProposer = listing2.getUserId() == user.getUserId();

            if (!isOwner && !isProposer) {
                req.getSession().setAttribute("error", "You are not part of this exchange.");
                resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
                return;
            }

            // Start transaction
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Revert both listings to AVAILABLE
            boolean listing1Reverted = listingDAO.updateListingStatus(p.getListingId(), "AVAILABLE");
            boolean listing2Reverted = listingDAO.updateListingStatus(p.getProposedListingId(), "AVAILABLE");

            if (listing1Reverted && listing2Reverted) {
                proposalDAO.updateProposalStatus(pid, "CANCELLED");

                // Notify the other party
                int otherUserId = isOwner ? listing2.getUserId() : listing1.getUserId();
                Notification notification = new Notification(
                    otherUserId,
                    "EXCHANGE_PROPOSAL",
                    "Exchange Cancelled",
                    user.getName() + " cancelled the exchange. Both listings are now available again.",
                    isOwner ? p.getListingId() : p.getProposedListingId()
                );
                notificationDAO.createNotification(notification);

                conn.commit();
                req.getSession().setAttribute("success", "Exchange cancelled. Both listings are now available again.");
            } else {
                conn.rollback();
                req.getSession().setAttribute("error", "Failed to cancel exchange.");
            }

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            req.getSession().setAttribute("error", "Failed to cancel exchange: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }

        resp.sendRedirect(req.getContextPath() + "/listings/my-listings");
    }
}