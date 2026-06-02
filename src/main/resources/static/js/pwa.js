// Registers the service worker so the app works offline and is installable.
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function () {
        navigator.serviceWorker.register('/service-worker.js').catch(function (err) {
            console.warn('Service worker registration failed:', err);
        });
    });
}
