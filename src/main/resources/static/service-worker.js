// Minimal offline-capable service worker for the PWA.
// Strategy: only *static* assets (CSS/JS/icons/manifest) are cached. Page
// navigations always hit the network so server-rendered data is current, and
// dynamic GET endpoints (e.g. /fixable/teachers) are never cached at all.

const CACHE = 'lfl-cache-v3';
const APP_SHELL = [
    '/offline',
    '/css/app.css',
    '/js/pwa.js',
    '/manifest.webmanifest',
    '/icons/icon.svg'
];

// Only these paths are safe to serve from the cache. Everything else is dynamic
// (rendered HTML pages, JSON endpoints) and must always come from the network.
function isStaticAsset(url) {
    return url.origin === self.location.origin && (
        url.pathname.startsWith('/css/') ||
        url.pathname.startsWith('/js/') ||
        url.pathname.startsWith('/icons/') ||
        url.pathname === '/manifest.webmanifest'
    );
}

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE).then(cache => cache.addAll(APP_SHELL)).then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys =>
            Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))
        ).then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', event => {
    const request = event.request;
    if (request.method !== 'GET') {
        return; // never cache mutations
    }

    // Page navigations: always try the network so the rendered data is current;
    // fall back to the cached offline page only when the network is unavailable.
    if (request.mode === 'navigate') {
        event.respondWith(
            fetch(request).catch(() => caches.match('/offline'))
        );
        return;
    }

    // Dynamic GETs (JSON endpoints, anything that isn't a static asset) are left
    // to the network untouched — caching them is what makes data look stale.
    if (!isStaticAsset(new URL(request.url))) {
        return;
    }

    // Static assets: stale-while-revalidate. Serve the cached copy immediately
    // (fast, offline-friendly) but always re-fetch in the background and update
    // the cache, so edited assets propagate on the next load.
    event.respondWith(
        caches.match(request).then(cached => {
            const network = fetch(request).then(response => {
                const copy = response.clone();
                caches.open(CACHE).then(cache => cache.put(request, copy));
                return response;
            }).catch(() => cached);
            return cached || network;
        })
    );
});
