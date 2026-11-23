/* ============ Stream Warrior offline cache (service worker) ============ */
"use strict";
/* eslint-disable no-restricted-globals */

const SW_CACHE = "stream-warrior-v28";
const SW_PRECACHE = ["./", "index.html", "icon.svg", "manifest.webmanifest"];

self.addEventListener("install", (e) => {
  self.skipWaiting();
  e.waitUntil(caches.open(SW_CACHE).then((c) => c.addAll(SW_PRECACHE)));
});

self.addEventListener("activate", (e) => {
  e.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.filter(k => k !== SW_CACHE).map(k => caches.delete(k)));
    await self.clients.claim();
  })());
});

/* Network-first for page navigations so updates go live immediately (falls back to cache offline).
   Cache-first for other same-origin GETs (icons, etc.); every successful fetch is stored for offline replay. */
self.addEventListener("fetch", (e) => {
  const req = e.request;
  if (req.method !== "GET") return;
  if (!req.url.startsWith(self.location.origin)) return; /* never touch AI API calls */

  if (req.mode === "navigate" || (req.destination === "document")) {
    e.respondWith(
      fetch(req).then((res) => {
        if (res && res.ok && res.type === "basic") {
          const copy = res.clone();
          caches.open(SW_CACHE).then((c) => c.put(req, copy));
        }
        return res;
      }).catch(() => caches.match(req).then((hit) => hit || caches.match("./")))
    );
    return;
  }

  e.respondWith(
    caches.match(req).then((hit) => hit || fetch(req).then((res) => {
      if (res && res.ok && res.type === "basic") {
        const copy = res.clone();
        caches.open(SW_CACHE).then((c) => c.put(req, copy));
      }
      return res;
    }).catch(() => caches.match("./")))
  );
});
