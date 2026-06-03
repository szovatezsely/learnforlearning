// Registers the service worker so the app works offline and is installable.
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function () {
        navigator.serviceWorker.register('/service-worker.js').catch(function (err) {
            console.warn('Service worker registration failed:', err);
        });
    });
}

// Highlights the navbar link that matches the current page.
window.addEventListener('DOMContentLoaded', function () {
    var path = window.location.pathname;
    document.querySelectorAll('.navbar .nav-link').forEach(function (link) {
        var href = link.getAttribute('href');
        if (!href || href === '#') return;
        var isActive = href === '/' ? path === '/' : path === href || path.startsWith(href + '/');
        if (isActive) {
            link.classList.add('active');
            link.setAttribute('aria-current', 'page');
        }
    });
});
