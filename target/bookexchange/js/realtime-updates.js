// src/main/webapp/js/realtime-updates.js
let lastNotificationCount = 0;
let lastMessageCount = 0;
let lastListingStates = {};
let hasShownUpdateToast = false; // Prevent multiple toasts on reload

const UPDATE_INTERVAL = 10000; // 10 seconds

document.addEventListener('DOMContentLoaded', () => {
    // Initialize from session storage to prevent duplicate notifications after reload
    const storedNotifCount = sessionStorage.getItem('lastNotificationCount');
    const storedMsgCount = sessionStorage.getItem('lastMessageCount');
    const storedListingStates = sessionStorage.getItem('lastListingStates');
    
    // Check if we just reloaded from an update
    const justReloaded = sessionStorage.getItem('justReloaded');
    if (justReloaded === 'true') {
        hasShownUpdateToast = true;
        sessionStorage.removeItem('justReloaded');
    }
    
    if (storedNotifCount) lastNotificationCount = parseInt(storedNotifCount);
    if (storedMsgCount) lastMessageCount = parseInt(storedMsgCount);
    if (storedListingStates) {
        try {
            lastListingStates = JSON.parse(storedListingStates);
        } catch (e) {
            lastListingStates = {};
        }
    }
    
    startRealtimeUpdates();
});

function startRealtimeUpdates() {
    updateCounts();
    setInterval(updateCounts, UPDATE_INTERVAL);

    if (window.location.pathname.includes('/my-listings')) {
        updateListingStates();
        setInterval(updateListingStates, UPDATE_INTERVAL);
    }

    if (window.location.pathname.includes('/view-listing.jsp') || window.location.pathname.includes('/view')) {
        const listingId = new URLSearchParams(window.location.search).get('id');
        if (listingId) {
            checkListingUpdates(listingId);
            setInterval(() => checkListingUpdates(listingId), UPDATE_INTERVAL);
        }
    }
}

function updateCounts() {
    // Notification count
    fetch(`${getContextPath()}/notifications/count`)
        .then(r => r.json())
        .then(data => {
            const increased = data.count > lastNotificationCount && lastNotificationCount > 0;
            lastNotificationCount = data.count;
            sessionStorage.setItem('lastNotificationCount', data.count);
            updateNotificationBadge(data.count);
            if (increased) showToast('New Notification', 'You have a new notification!', 'info');
        })
        .catch(err => console.error('Error fetching notification count:', err));

    // Message count
    fetch(`${getContextPath()}/api/messages/unread-count`)
        .then(r => r.json())
        .then(data => {
            if (data.count !== lastMessageCount) {
                lastMessageCount = data.count;
                sessionStorage.setItem('lastMessageCount', data.count);
                updateMessageBadge(data.count);
            }
        })
        .catch(err => console.error('Error fetching message count:', err));
}

function updateNotificationBadge(count) {
    let badge = document.getElementById('notificationBadge');
    if (!badge) {
        const bell = document.getElementById('notificationBell');
        if (bell) {
            badge = document.createElement('span');
            badge.id = 'notificationBadge';
            badge.className = 'badge bg-danger position-absolute top-0 start-100 translate-middle';
            bell.style.position = 'relative';
            bell.appendChild(badge);
        }
    }
    if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'block' : 'none';
    }
}

function updateMessageBadge(count) {
    const link = document.querySelector('a[href*="messages/inbox"], a[href*="messages"]');
    if (!link) return;
    let badge = link.querySelector('.badge');
    if (!badge && count > 0) {
        badge = document.createElement('span');
        badge.className = 'badge bg-danger';
        link.appendChild(badge);
    }
    if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline' : 'none';
    }
}

function updateListingStates() {
    fetch(`${getContextPath()}/api/my-listings-status`)
        .then(r => r.json())
        .then(listings => {
            let changed = false;
            let changedListings = [];
            
            listings.forEach(l => {
                if (lastListingStates[l.id] && lastListingStates[l.id] !== l.status) {
                    changed = true;
                    changedListings.push(l.id);
                }
                lastListingStates[l.id] = l.status;
            });
            
            // Only show toast and reload if something actually changed AND we haven't already shown it
            if (changed && !hasShownUpdateToast) {
                hasShownUpdateToast = true;
                sessionStorage.setItem('lastListingStates', JSON.stringify(lastListingStates));
                sessionStorage.setItem('justReloaded', 'true');
                showToast('Listings Updated', 'One of your listings has changed status. Refreshing...', 'success');
                setTimeout(() => location.reload(), 2000);
            }
        })
        .catch(err => console.error('Error fetching listing states:', err));
}

function checkListingUpdates(listingId) {
    const statusElement = document.querySelector('.listing-status, .badge');
    const currentStatus = statusElement ? statusElement.textContent.trim() : '';
    
    fetch(`${getContextPath()}/api/listing-status?id=${listingId}`)
        .then(r => r.json())
        .then(data => {
            // Only reload if status actually changed
            if (data.status && currentStatus && data.status !== currentStatus) {
                sessionStorage.setItem('justReloaded', 'true');
                showToast('Listing Updated', 'This listing has been updated. Refreshing...', 'info');
                setTimeout(() => location.reload(), 2000);
            }
        })
        .catch(err => console.error('Error checking listing updates:', err));
}

function showToast(title, message, type = 'info') {
    const container = getOrCreateToastContainer();
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <strong>${title}</strong><br>${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>`;
    container.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast, { delay: 3000 }); // 3 seconds
    bsToast.show();
    toast.addEventListener('hidden.bs.toast', () => toast.remove());
}

function getOrCreateToastContainer() {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }
    return container;
}

function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
}