/**
 * The Work Code - Central API client (demo).
 *
 * - Talks to the API gateway at TWC_API_BASE (default http://localhost:8080).
 * - Stores the demo JWT in localStorage after persona login.
 * - Unwraps the standard { success, data, requestId } envelope.
 * - USE_DEMO_DATA=true (default) keeps every screen functional with static
 *   Stitch content when the backend is unreachable, so demos never break.
 * - Never logs or stores passwords; tokens are demo JWTs.
 */
(function (global) {
  'use strict';

  var CONFIG = {
    API_BASE: (global.TWC_API_BASE || window.location.origin.replace(/:3000$/, ':8080') || 'http://localhost:8080'),
    USE_DEMO_DATA: (typeof global.TWC_USE_DEMO_DATA === 'boolean') ? global.TWC_USE_DEMO_DATA : true,
    TOKEN_KEY: 'twc.token',
    USER_KEY: 'twc.user'
  };

  var DEMO_PERSONAS = {
    admin: { username: 'admin', role: 'ADMIN', name: 'Alex Morgan', org: 'ORG-8821' },
    analyst: { username: 'analyst', role: 'ANALYST', name: 'Sam Whitfield', org: 'ORG-8821' },
    developer: { username: 'developer', role: 'DEVELOPER', name: 'Devon Park', org: 'ORG-8821' },
    viewer: { username: 'viewer', role: 'VIEWER', name: 'Riley Chen', org: 'ORG-8821' }
  };

  function token() {
    try { return localStorage.getItem(CONFIG.TOKEN_KEY); } catch (e) { return null; }
  }

  function user() {
    try {
      var raw = localStorage.getItem(CONFIG.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (e) { return null; }
  }

  function setSession(tokenValue, userValue) {
    try {
      localStorage.setItem(CONFIG.TOKEN_KEY, tokenValue);
      localStorage.setItem(CONFIG.USER_KEY, JSON.stringify(userValue));
    } catch (e) { /* private mode */ }
  }

  function clearSession() {
    try {
      localStorage.removeItem(CONFIG.TOKEN_KEY);
      localStorage.removeItem(CONFIG.USER_KEY);
    } catch (e) { /* noop */ }
  }

  function requestId() {
    return 'REQ-ui-' + Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }

  /**
   * Core request. Resolves with parsed `data` from the envelope.
   * Rejects with { code, message, requestId, status } on API errors.
   */
  function request(method, path, body, options) {
    options = options || {};
    var headers = { 'X-Request-ID': requestId() };
    if (body !== undefined && body !== null && !(body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }
    var t = token();
    if (t) { headers['Authorization'] = 'Bearer ' + t; }

    return fetch(CONFIG.API_BASE + path, {
      method: method,
      headers: headers,
      body: body instanceof FormData ? body : (body === undefined ? undefined : JSON.stringify(body))
    }).then(function (res) {
      if (res.status === 401) {
        clearSession();
        if (!options.silent) { showToast('Session expired - sign in again.', 'error'); }
        throw { code: 'UNAUTHORIZED', message: 'Authentication required.', status: 401 };
      }
      if (res.status === 403) {
        if (!options.silent) { showToast('You do not have permission for this action.', 'error'); }
        throw { code: 'FORBIDDEN', message: 'Permission denied.', status: 403 };
      }
      if (res.status === 404) {
        throw { code: 'NOT_FOUND', message: 'Record not found.', status: 404 };
      }
      return res.json().catch(function () { return {}; }).then(function (envelope) {
        if (!envelope || typeof envelope.success !== 'boolean') {
          return envelope; // non-envelope response (e.g. PDF)
        }
        if (!envelope.success) {
          var err = envelope.error || {};
          throw { code: err.code || 'ERROR', message: err.message || 'Request failed.',
                  details: err.details, requestId: envelope.requestId, status: res.status };
        }
        return envelope.data;
      });
    });
  }

  var api = {
    CONFIG: CONFIG,
    DEMO_PERSONAS: DEMO_PERSONAS,
    token: token,
    user: user,
    setSession: setSession,
    clearSession: clearSession,

    get: function (path, options) { return request('GET', path, undefined, options); },
    post: function (path, body, options) { return request('POST', path, body, options); },
    put: function (path, body, options) { return request('PUT', path, body, options); },

    /** POST raw FormData (batch CSV upload). */
    upload: function (path, formData) { return request('POST', path, formData, {}); },

    /** Login with a demo persona key (admin/analyst/developer/viewer). */
    loginPersona: function (key) {
      var persona = DEMO_PERSONAS[key];
      if (!persona) { return Promise.reject({ message: 'Unknown persona' }); }
      return this.post('/api/v1/auth/login', { username: persona.username }, { silent: true })
        .then(function (data) {
          var u = data.user || persona;
          // Gateway returns organizationId; the UI reads org. Normalize both.
          if (u && !u.org && u.organizationId) { u.org = u.organizationId; }
          setSession(data.token, u);
          return data;
        })
        .catch(function (err) {
          if (CONFIG.USE_DEMO_DATA) {
            // Offline demo fallback: issue a pseudo-session so screens stay navigable.
            setSession('demo.' + persona.username + '.token', persona);
            return { demoFallback: true, user: persona };
          }
          throw err;
        });
    },

    logout: function () {
      clearSession();
      showToast('Signed out.', 'info');
    },

    /** Human-readable error message from a thrown API error. */
    errorMessage: function (err) {
      if (!err) { return 'Unknown error'; }
      return err.message || err.code || 'Request failed';
    },

    /** Optimistic navigation guard: redirect to sign-in when no session. */
    requireSession: function () {
      if (!token()) {
        window.location.href = '/stitch-screens/sign_in_the_work_code/code.html';
        return false;
      }
      return true;
    }
  };

  // ---------------- Toasts ----------------
  function showToast(message, kind) {
    var container = document.getElementById('toastContainer');
    if (!container) { return; }
    var el = document.createElement('div');
    el.className = 'twc-toast twc-toast-' + (kind || 'info');
    el.textContent = message;
    container.appendChild(el);
    setTimeout(function () { el.remove(); }, 4200);
  }
  api.showToast = showToast;

  // ---------------- Demo mode banner (injected on every screen) ----------------
  function injectDemoBanner() {
    if (document.getElementById('twc-demo-banner')) { return; }
    var banner = document.createElement('div');
    banner.id = 'twc-demo-banner';
    banner.style.cssText = 'position:fixed;bottom:12px;left:12px;z-index:9999;display:flex;gap:8px;align-items:center;' +
      'background:#0b192c;color:#e2e8f0;font:600 11px/1 Inter,sans-serif;letter-spacing:.06em;' +
      'padding:8px 12px;border-radius:6px;border:1px solid #1e2c48;box-shadow:0 4px 12px rgba(15,23,42,.25);';
    var dot = document.createElement('span');
    dot.style.cssText = 'width:7px;height:7px;border-radius:99px;background:#f59e0b;display:inline-block;';
    banner.appendChild(dot);
    banner.appendChild(document.createTextNode('DEMO MODE — SYNTHETIC DATA'));
    var userChip = user();
    if (userChip) {
      var chip = document.createElement('span');
      chip.style.cssText = 'color:#89f5e7;margin-left:6px;';
      chip.textContent = userChip.name ? ('| ' + userChip.name + ' (' + (userChip.role || '') + ')') : '';
      banner.appendChild(chip);
    }
    document.body.appendChild(banner);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', injectDemoBanner);
  } else {
    injectDemoBanner();
  }

  global.TWC = api;
})(window);
