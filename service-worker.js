const CACHE_VERSION = 'v3';
const SHELL_CACHE = `cladar-shell-${CACHE_VERSION}`;
const REMOTE_CACHE = `cladar-remote-${CACHE_VERSION}`;
const APP_ROOT = new URL('./', self.location.href);
const APP_URL = new URL('cladar.html', APP_ROOT).href;
const SHELL_URLS = [
  APP_ROOT.href,
  APP_URL,
  new URL('manifest.webmanifest', APP_ROOT).href,
 new URL('app/src/main/assets/icons/cladar-192.png', APP_ROOT).href,
  new URL('app/src/main/assets/icons/cladar-512.png', APP_ROOT).href,
  new URL('app/src/main/assets/icons/apple-touch-icon.png', APP_ROOT).href,
  new URL('app/src/main/assets/vendor/qrcode.min.js', APP_ROOT).href
];
const REMOTE_ORIGINS = new Set([
  'https://cdn.jsdelivr.net',
  'https://fonts.googleapis.com',
  'https://fonts.gstatic.com'
]);
const REMOTE_URLS = [
  'https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600;700&family=Inter:wght@400;500;600;700;800&display=swap',
  'https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css',
  'https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js',
  'https://cdn.jsdelivr.net/npm/marked/marked.min.js',
  'https://cdn.jsdelivr.net/npm/jspdf@3.0.1/dist/jspdf.umd.min.js',
  'https://cdn.jsdelivr.net/npm/jspdf-autotable@5.0.2/dist/jspdf.plugin.autotable.min.js'
];
const MAX_REMOTE_ENTRIES = 80;

async function cacheRemoteResource(cache, url, depth = 0) {
  const resourceUrl = new URL(url);
  if (!REMOTE_ORIGINS.has(resourceUrl.origin)) return;

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 8000);
  let response;
  try {
    response = await fetch(resourceUrl.href, {
      mode: 'cors',
      credentials: 'omit',
      signal: controller.signal
    });
  } catch {
    return;
  } finally {
    clearTimeout(timeoutId);
  }

  if (!response.ok) return;
  try {
    await cache.put(resourceUrl.href, response.clone());
  } catch (error) {
    console.warn('Unable to cache an optional Cladar resource:', resourceUrl.href, error);
    return;
  }

  if (depth >= 1 || !(response.headers.get('content-type') || '').includes('text/css')) return;
  const css = await response.text();
  const linkedResources = [...css.matchAll(/url\(\s*(['"]?)(.*?)\1\s*\)/g)]
    .map(match => match[2].trim())
    .filter(reference => reference && !reference.startsWith('data:'))
    .map(reference => new URL(reference, resourceUrl).href);
  await Promise.allSettled(linkedResources.map(reference => cacheRemoteResource(cache, reference, depth + 1)));
}

async function trimRemoteCache(cache) {
  const keys = await cache.keys();
  const overflow = keys.length - MAX_REMOTE_ENTRIES;
  if (overflow > 0) {
    await Promise.all(keys.slice(0, overflow).map(request => cache.delete(request)));
  }
}

self.addEventListener('install', event => {
  event.waitUntil((async () => {
    const shell = await caches.open(SHELL_CACHE);
    await shell.addAll(SHELL_URLS);

    const remote = await caches.open(REMOTE_CACHE);
    await Promise.allSettled(REMOTE_URLS.map(url => cacheRemoteResource(remote, url)));
    await trimRemoteCache(remote);
    await self.skipWaiting();
  })());
});

self.addEventListener('activate', event => {
  event.waitUntil((async () => {
    const cacheNames = await caches.keys();
    await Promise.all(cacheNames
      .filter(name => name.startsWith('cladar-') && name !== SHELL_CACHE && name !== REMOTE_CACHE)
      .map(name => caches.delete(name)));
    await self.clients.claim();
  })());
});

self.addEventListener('fetch', event => {
  const request = event.request;
  if (request.method !== 'GET') return;

  const requestUrl = new URL(request.url);
  if (requestUrl.origin !== self.location.origin) {
    if (!REMOTE_ORIGINS.has(requestUrl.origin)) return;
    event.respondWith((async () => {
      const remote = await caches.open(REMOTE_CACHE);
      const cached = await remote.match(request, { ignoreVary: true });
      if (cached) return cached;

      const response = await fetch(request);
      if (response.ok) {
        try {
          await remote.put(request, response.clone());
          await trimRemoteCache(remote);
        } catch (error) {
          console.warn('Unable to cache an optional Cladar response:', request.url, error);
        }
      }
      return response;
    })().catch(() => caches.match(request, { ignoreVary: true }).then(cached => cached || Response.error())));
    return;
  }

  if (request.mode === 'navigate') {
    event.respondWith((async () => {
      try {
        const response = await fetch(request);
        if (response.ok) {
          const shell = await caches.open(SHELL_CACHE);
          try {
            await shell.put(request, response.clone());
            if (requestUrl.pathname === new URL(APP_URL).pathname) {
              await shell.put(APP_URL, response.clone());
            }
          } catch (error) {
            console.warn('Unable to update the cached Cladar page:', error);
          }
        }
        return response;
      } catch {
        return (await caches.match(request)) || (await caches.match(APP_URL)) || Response.error();
      }
    })());
    return;
  }

  event.respondWith((async () => {
    const cached = await caches.match(request);
    if (cached) return cached;
    try {
      return await fetch(request);
    } catch {
      return Response.error();
    }
  })());
});
