// Minimal offline-capable service worker for the PWA.
// Strategy: cache the app shell on install; serve cached assets when offline and
// fall back to the offline page for navigations that fail.

const CACHE = 'lfl-cache-v1';
const APP_SHELL = [
    '/',
    '/offline',
    '/css/app.css',
    '/js/pwa.js',
    '/manifest.webmanifest',
    '/icons/icon.svg'
];

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

    // For page navigations: try the network, fall back to the offline page.
    if (request.mode === 'navigate') {
        event.respondWith(
            fetch(request).catch(() => caches.match('/offline'))
        );
        return;
    }

    // For other GETs (assets): cache-first, then network.
    event.respondWith(
        caches.match(request).then(cached => cached || fetch(request).then(response => {
            const copy = response.clone();
            caches.open(CACHE).then(cache => cache.put(request, copy));
            return response;
        }).catch(() => cached))
    );
});
