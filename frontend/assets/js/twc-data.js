/**
 * The Work Code - shared data helpers for Stitch screens.
 *
 * Renders live API data into the static Stitch tables/cards. Each screen
 * includes this after twc-api.js and calls TWC_DATA.wire({...}) with small
 * per-screen adapters. Every helper degrades gracefully: when the backend is
 * unreachable (demo fallback) the original static Stitch content stays.
 *
 * Include order:
 *   <script src="/assets/js/twc-api.js"></script>
 *   <script src="/assets/js/twc-data.js"></script>
 */
(function (global) {
  'use strict';

  function esc(v) {
    return String(v == null ? '' : v)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function fmtDate(iso) {
    if (!iso) { return '—'; }
    var d = new Date(iso);
    if (isNaN(d.getTime())) { return String(iso); }
    return d.toISOString().slice(0, 10) + ' ' + d.toISOString().slice(11, 16);
  }

  function fmtMoney(n) {
    if (n == null) { return '—'; }
    return '₹' + Number(n).toLocaleString('en-IN');
  }

  var STATUS_STYLES = {
    COMPLETED: 'bg-secondary-container text-on-secondary-container',
    VERIFIED: 'bg-secondary-container text-on-secondary-container',
    SUCCESS: 'bg-secondary-container text-on-secondary-container',
    MATCH: 'bg-secondary-container text-on-secondary-container',
    ACTIVE: 'bg-secondary-container text-on-secondary-container',
    UP: 'bg-secondary-container text-on-secondary-container',
    PENDING: 'bg-primary-fixed text-on-primary-fixed',
    PROCESSING: 'bg-primary-fixed text-on-primary-fixed',
    REVIEW_REQUIRED: 'bg-tertiary-fixed text-on-tertiary-fixed',
    UNDER_REVIEW: 'bg-tertiary-fixed text-on-tertiary-fixed',
    REVIEW: 'bg-tertiary-fixed text-on-tertiary-fixed',
    SUBMITTED: 'bg-primary-fixed text-on-primary-fixed',
    FAILED: 'bg-error-container text-on-error-container',
    NO_MATCH: 'bg-error-container text-on-error-container',
    NO_HIT: 'bg-surface-container-high text-on-surface-variant',
    NOT_VERIFIED: 'bg-error-container text-on-error-container',
    DOWN: 'bg-error-container text-on-error-container',
    REVOKED: 'bg-error-container text-on-error-container',
    TERMINATED: 'bg-surface-container-high text-on-surface-variant',
    INACTIVE: 'bg-surface-container-high text-on-surface-variant'
  };

  function badge(status) {
    var s = String(status || '—').toUpperCase();
    var cls = STATUS_STYLES[s] || 'bg-surface-container-high text-on-surface-variant';
    return '<span class="px-2 py-0.5 rounded-full font-data-mono-sm text-data-mono-sm ' + cls + '">' + esc(s) + '</span>';
  }

  function detectTbody() {
    return document.querySelector('tbody');
  }

  /** Show a subtle offline notice instead of silently doing nothing. */
  function notice(msg) {
    var el = document.getElementById('twc-data-notice');
    if (!el) {
      el = document.createElement('div');
      el.id = 'twc-data-notice';
      el.style.cssText = 'position:fixed;bottom:48px;right:12px;z-index:9999;' +
        'background:#0b192c;color:#e2e8f0;font:600 11px/1.4 Inter,sans-serif;padding:8px 12px;' +
        'border-radius:6px;border:1px solid #1e2c48;max-width:280px;';
      (document.body || document.documentElement).appendChild(el);
    }
    el.textContent = msg;
    el.style.display = 'block';
    setTimeout(function () { el.style.display = 'none'; }, 4000);
  }

  /**
   * Replace the rows of the first <tbody> with generated <tr> HTML.
   * headers: array of <th> innerText to keep the static header aligned.
   */
  function fillTable(rowsHtml, opts) {
    opts = opts || {};
    var tbody = opts.tbody || detectTbody();
    if (!tbody) { return false; }
    if (rowsHtml) {
      tbody.innerHTML = rowsHtml;
      return true;
    }
    return false;
  }

  function emptyRow(cols, message) {
    return '<tr><td colspan="' + cols + '" class="text-center py-8 text-on-surface-variant font-body-sm">' +
      esc(message || 'No records found.') + '</td></tr>';
  }

  global.TWC_DATA = {
    esc: esc,
    fmtDate: fmtDate,
    fmtMoney: fmtMoney,
    badge: badge,
    fillTable: fillTable,
    emptyRow: emptyRow,
    notice: notice,
    detectTbody: detectTbody
  };
})(window);
