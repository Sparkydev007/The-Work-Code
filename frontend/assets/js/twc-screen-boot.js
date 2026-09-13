/**
 * The Work Code - shared screen boot.
 * Include after twc-api.js (+ twc-data.js) on every Stitch screen:
 *   <script src="/assets/js/twc-api.js"></script>
 *   <script src="/assets/js/twc-data.js"></script>
 *   <script src="/assets/js/twc-screen-boot.js"></script>
 *
 * Provides: session guard, persona display, sidebar navigation, sign-out,
 * and live data wiring per screen (auto-detected from the URL).
 */
(function (global) {
  'use strict';

  var SCREENS = {
    'dashboard': 'main_operations_dashboard',
    'new-verification': 'initiate_new_verification',
    'verification-requests': 'verification_requests',
    'employees-directory': 'employees_directory',
    'employers-network': 'the_work_code_fully_interactive_operations_platform',
    'income-verification': 'income_verification',
    'verification-reports': 'verification_reports',
    'risk-fraud-analytics': 'risk_fraud_analytics',
    'anomalies': 'anomalies',
    'api-keys-logs': 'api_keys_logs',
    'webhooks': 'webhooks',
    'audit-logs': 'audit_logs',
    'security-center': 'security_center',
    'documentation-support': 'api_docs'
  };

  var ROLE_TITLES = {
    ADMIN: 'Chief Verification Officer',
    ANALYST: 'Verification Analyst',
    DEVELOPER: 'Platform Developer',
    VIEWER: 'Read-only Observer'
  };

  var D = function () { return global.TWC_DATA; };
  var A = function () { return global.TWC; };

  // ------------------------------------------------------------------
  // boot
  // ------------------------------------------------------------------
  function boot() {
    if (!A()) { return; } // opened as raw file: leave static Stitch content

    // 1. Session guard
    if (!TWC.token()) {
      window.location.replace('/stitch-screens/sign_in_the_work_code/code.html');
      return;
    }

    renderPersona();
    wireSidebar();
    wireSignOut();
    wireScreenData();
  }

  function renderPersona() {
    var user = TWC.user();
    if (!user) { return; }
    var divs = document.querySelectorAll('aside .truncate > div');
    if (divs.length >= 3) {
      divs[0].textContent = user.name || 'Demo User';
      divs[1].textContent = ROLE_TITLES[user.role] || (user.role || 'Member');
      divs[2].textContent = user.org || 'ORG-8821';
    }
  }

  function wireSidebar() {
    document.querySelectorAll('[data-path]').forEach(function (el) {
      if (el.tagName !== 'A') { return; }
      el.addEventListener('click', function (ev) {
        ev.preventDefault();
        var path = el.getAttribute('data-path');
        if (SCREENS[path]) {
          window.location.href = '/stitch-screens/' + SCREENS[path] + '/code.html';
        } else {
          TWC.showToast('That screen is not part of the demo set yet.', 'info');
        }
      });
    });
  }

  function wireSignOut() {
    var btn = document.querySelector('aside .truncate ~ button');
    if (btn) {
      btn.addEventListener('click', function (ev) {
        ev.stopPropagation();
        TWC.logout();
        window.location.href = '/stitch-screens/sign_in_the_work_code/code.html';
      });
    }
  }

  // ------------------------------------------------------------------
  // per-screen live data wiring
  // ------------------------------------------------------------------
  function wireScreenData() {
    var path = window.location.pathname;
    var screen = (path.match(/stitch-screens\/([^/]+)/) || [])[1] || '';

    switch (screen) {
      case 'main_operations_dashboard': wireDashboard(); break;
      case 'verification_requests': wireRequests(); break;
      case 'employees_directory': wireEmployees(); break;
      case 'audit_logs': wireAudit(); break;
      case 'webhooks': wireWebhooks(); break;
      case 'income_verification': wireIncome(); break;
      // note: anomalies handled below with its row-card layout
      case 'verification_reports': wireReports(); break;
      case 'risk_fraud_analytics': wireRisk(); break;
      case 'anomalies': wireAnomalies(); break;
      case 'initiate_new_verification': wireInitiate(); break;
      default: break; // screens without live data stay as designed
    }
  }

  // ------------------------------------------------------------------
  // dashboard KPIs
  // ------------------------------------------------------------------
  function wireDashboard() {
    function setKpi(i, v) {
      var spans = document.querySelectorAll('span.font-display-lg.text-display-lg');
      if (spans.length > i) { spans[i].textContent = v; }
    }
    Promise.all([
      TWC.get('/api/v1/verifications?size=1', { silent: true }).catch(function () { return null; }),
      TWC.get('/api/v1/verifications?status=COMPLETED&size=1', { silent: true }).catch(function () { return null; }),
      TWC.get('/api/v1/verifications?status=PENDING&size=1', { silent: true }).catch(function () { return null; }),
      TWC.get('/api/v1/verifications?status=REVIEW_REQUIRED&size=1', { silent: true }).catch(function () { return null; }),
      TWC.get('/api/v1/risk/anomalies?size=1', { silent: true }).catch(function () { return null; })
    ]).then(function (r) {
      if (!r[0]) { return; }
      setKpi(0, (r[0].totalElements || 0).toLocaleString());
      setKpi(1, (r[1] ? r[1].totalElements : 0).toLocaleString());
      setKpi(2, (r[2] ? r[2].totalElements : 0).toLocaleString());
      setKpi(3, (r[3] ? r[3].totalElements : 0).toLocaleString());
      var pill = document.querySelector('header .font-data-mono-sm.text-data-mono-sm');
      if (pill) {
        var total = r[0].totalElements || 0;
        var done = r[1] ? r[1].totalElements : 0;
        pill.textContent = 'Status: ' + (total ? Math.round(100 * done / total) : 100) + '% Verified | ' + total + ' requests';
      }
    });
  }

  // ------------------------------------------------------------------
  // verification requests table
  // cols: [checkbox] Request ID | Subject | Employer | Type | Status |
  //       Requested | SLA Window | Assignee | Actions
  // ------------------------------------------------------------------
  function wireRequests() {
    TWC.get('/api/v1/verifications?page=0&size=15', { silent: true }).then(function (page) {
      var rows = (page.content || []).map(function (v) {
        return '<tr>' +
          '<td class="px-space-md py-3 text-center"><input type="checkbox" class="w-3.5 h-3.5 rounded border-slate-300"/></td>' +
          '<td class="px-space-md py-3 font-data-mono-sm text-data-mono-sm text-primary">' + D().esc(v.verificationCode) + '</td>' +
          '<td class="px-space-md py-3">' + D().esc(v.applicantName) + '</td>' +
          '<td class="px-space-md py-3">' + D().esc(v.employerName || '—') + '</td>' +
          '<td class="px-space-md py-3 font-data-mono-sm text-data-mono-sm">' + D().esc(String(v.verificationType || '').replace(/_/g, ' ')) + '</td>' +
          '<td class="px-space-md py-3">' + D().badge(v.status) + '</td>' +
          '<td class="px-space-md py-3 font-data-mono-sm text-data-mono-sm">' + D().fmtDate(v.createdAt) + '</td>' +
          '<td class="px-space-md py-3 font-data-mono-sm text-data-mono-sm">' + (v.status === 'COMPLETED' ? 'Met' : '48h window') + '</td>' +
          '<td class="px-space-md py-3">' + D().esc(v.requestedBy || 'auto') + '</td>' +
          '<td class="px-space-md py-3 text-right"><a href="/stitch-screens/verification_detail_dossier_ver_100293/code.html" class="text-primary text-body-sm font-semibold hover:underline">View</a></td>' +
          '</tr>';
      }).join('');
      D().fillTable(rows || D().emptyRow(10));
    }).catch(function () {
      D().notice('Backend unreachable — showing demo data.');
    });
  }

  // ------------------------------------------------------------------
  // employees directory (grid of cards)
  // ------------------------------------------------------------------
  function wireEmployees() {
    var grid = document.querySelector('main .grid');
    if (!grid) { return; }
    TWC.get('/api/v1/employees?page=0&size=12', { silent: true }).then(function (page) {
      var cards = (page.content || []).map(function (e) {
        return '<div class="rounded border border-outline-variant/40 bg-surface-container-lowest p-space-md space-y-1.5">' +
          '<div class="flex items-center justify-between gap-2">' +
          '<span class="font-headline-sm text-body-md text-on-surface">' + D().esc(e.name) + '</span>' +
          D().badge(e.status) + '</div>' +
          '<div class="font-data-mono-sm text-data-mono-sm text-primary">' + D().esc(e.employeeNumber) + '</div>' +
          '<div class="font-body-sm text-body-sm text-on-surface-variant">' + D().esc(e.jobTitle || '') + ' · ' + D().esc(e.department || '') + '</div>' +
          '<div class="font-body-sm text-body-sm text-tertiary">' + D().esc(e.employerName || '') + '</div>' +
          '<div class="font-body-sm text-body-sm text-tertiary">Hired ' + D().esc(e.hireDate || '—') + '</div>' +
          '</div>';
      }).join('');
      if (cards) { grid.innerHTML = cards; }
    }).catch(function () {
      D().notice('Backend unreachable — showing demo data.');
    });
  }

  // ------------------------------------------------------------------
  // audit log table
  // cols: Timestamp | Actor/Role | Type | Action | Resource ID | Origin | Actions
  // ------------------------------------------------------------------
  function wireAudit() {
    TWC.get('/api/v1/audit?page=0&size=20', { silent: true }).then(function (page) {
      var rows = (page.content || []).map(function (a) {
        return '<tr>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().fmtDate(a.eventTime) + '</td>' +
          '<td class="py-2.5 px-space-md">' + D().esc(a.actor) + ' / ' + D().esc(a.actorRole || '') + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().esc(a.resourceType || '') + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().esc(a.action) + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().esc(a.resourceId || '') + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().esc(a.requestId || '') + '</td>' +
          '<td class="py-2.5 px-space-md text-right">' + D().badge(a.result) + '</td>' +
          '</tr>';
      }).join('');
      D().fillTable(rows || D().emptyRow(7));
    }).catch(function () {
      D().notice('Backend unreachable — showing demo data.');
    });
  }

  // ------------------------------------------------------------------
  // webhooks table
  // cols: Endpoint URL & Secret | Events | Status | Last Delivery | Success Rate | Actions
  // ------------------------------------------------------------------
  function wireWebhooks() {
    TWC.get('/api/v1/webhooks', { silent: true }).then(function (list) {
      var rows = (list || []).map(function (w) {
        return '<tr>' +
          '<td class="py-2.5 px-space-md"><div class="font-body-sm text-body-sm text-on-surface">' + D().esc(w.url) + '</div>' +
          '<div class="font-data-mono-sm text-data-mono-sm text-tertiary">' + D().esc(w.secretHint || '') + '</div></td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().esc(w.events) + '</td>' +
          '<td class="py-2.5 px-space-md">' + D().badge(w.status) + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().fmtDate(w.updatedAt) + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">—</td>' +
          '<td class="py-2.5 px-space-md text-right font-body-sm text-body-sm"><a href="#" onclick="return false;" class="text-primary hover:underline">Test</a></td>' +
          '</tr>';
      }).join('');
      D().fillTable(rows || D().emptyRow(6));
    }).catch(function () {
      D().notice('Backend unreachable — showing demo data.');
    });
  }

  // ------------------------------------------------------------------
  // income verification table + summary strip
  // ------------------------------------------------------------------
  function wireIncome() {
    TWC.get('/api/v1/verifications?page=0&size=50', { silent: true }).catch(function () { return null; })
      .then(function (page) {
        if (!page) { return null; }
        // Income screen shows verifications that include an income component
        // (INCOME or EMPLOYMENT_AND_INCOME); the API filter is exact-match,
        // so filter client-side.
        var rows = (page.content || [])
          .filter(function (v) { return String(v.verificationType || '').indexOf('INCOME') !== -1; })
          .map(function (v) {
          return '<tr>' +
            '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm text-primary">' + D().esc(v.verificationCode) + '</td>' +
            '<td class="py-2.5 px-space-md">' + D().esc(v.applicantName) + '</td>' +
            '<td class="py-2.5 px-space-md">' + D().esc(v.employerName || '—') + '</td>' +
            '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">Last 12 months</td>' +
            '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">—</td>' +
            '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">—</td>' +
            '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">—</td>' +
            '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + (v.confidence != null ? Math.round(v.confidence * 100) + '%' : '—') + '</td>' +
            '<td class="py-2.5 px-space-md">' + D().badge(v.status) + '</td>' +
            '<td class="py-2.5 px-space-md text-right font-body-sm text-body-sm"><a href="/stitch-screens/verification_detail_dossier_ver_100293/code.html" class="text-primary hover:underline">Open</a></td>' +
            '</tr>';
        }).join('');
        D().fillTable(rows || D().emptyRow(10));
        return null;
      });
  }

  // ------------------------------------------------------------------
  // verification reports table (list of generated PDF reports)
  // ------------------------------------------------------------------
  function wireReports() {
    TWC.get('/api/v1/reports?page=0&size=12', { silent: true }).then(function (page) {
      var rows = (page.content || []).map(function (r) {
        return '<tr>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm text-primary">' + D().esc(r.reportCode) + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().esc(r.verificationCode || r.verificationId || '') + '</td>' +
          '<td class="py-2.5 px-space-md">' + D().esc(r.applicantName || '') + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().esc(String(r.reportType || '').replace(/_/g, ' ')) + '</td>' +
          '<td class="py-2.5 px-space-md">' + D().badge(r.result || 'GENERATED') + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().fmtDate(r.generatedAt) + '</td>' +
          '<td class="py-2.5 px-space-md text-right font-body-sm text-body-sm">' +
          '<a href="' + (TWC.CONFIG.API_BASE) + '/api/v1/reports/' + encodeURIComponent(r.reportCode) + '/download" target="_blank" rel="noopener" class="text-primary hover:underline">Download PDF</a></td>' +
          '</tr>';
      }).join('');
      // reports screen has 7 columns in its static table; if the header differs,
      // the row still renders (browsers pad missing cells).
      D().fillTable(rows || D().emptyRow(7));
    }).catch(function () {
      D().notice('Backend unreachable — showing demo data.');
    });
  }

  // Report screen header is "Month | Total Queries | ..." — put codes in Month col.
  // Handled by column count above; visual mapping documented in STITCH_INTEGRATION.md.

  // ------------------------------------------------------------------
  // risk & fraud analytics table
  // cols: Request ID | Subject | Risk Score | Primary Anomaly | Detection Time | Adjudication | Action
  // ------------------------------------------------------------------
  function wireRisk() {
    TWC.get('/api/v1/risk/anomalies?page=0&size=15', { silent: true }).then(function (page) {
      var rows = (page.content || []).map(function (a) {
        return '<tr>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm text-primary">' + D().esc(a.verificationId || a.employeeNumber || '—') + '</td>' +
          '<td class="py-2.5 px-space-md">' + D().esc(a.title) + '</td>' +
          '<td class="py-2.5 px-space-md">' + D().badge(a.severity) + '</td>' +
          '<td class="py-2.5 px-space-md font-body-sm text-body-sm">' + D().esc(a.explanation || '') + '</td>' +
          '<td class="py-2.5 px-space-md font-data-mono-sm text-data-mono-sm">' + D().fmtDate(a.detectedAt) + '</td>' +
          '<td class="py-2.5 px-space-md">' + D().badge(a.status) + '</td>' +
          '<td class="py-2.5 px-space-md text-right font-body-sm text-body-sm"><a href="#" onclick="return false;" class="text-primary hover:underline">Review</a></td>' +
          '</tr>';
      }).join('');
      D().fillTable(rows || D().emptyRow(7));
    }).catch(function () {
      D().notice('Backend unreachable — showing demo data.');
    });
  }

  // Anomalies screen uses row cards (no table): rebuild the row container.
  function wireAnomalies() {
    var rowsRoot = null;
    document.querySelectorAll('main .flex.flex-col').forEach(function (el) {
      if (!rowsRoot && el.querySelector('.p-gutter-lg')) { rowsRoot = el; }
    });
    if (!rowsRoot) { return; }
    TWC.get('/api/v1/risk/anomalies?page=0&size=15', { silent: true }).then(function (page) {
      var rows = (page.content || []).map(function (a) {
        var sev = String(a.severity || 'INFO').toUpperCase();
        var dot = sev === 'CRITICAL' || sev === 'HIGH' ? 'bg-error' : sev === 'MEDIUM' ? 'bg-primary' : 'bg-secondary';
        return '<div class="p-gutter-lg hover:bg-surface-container-low/60 transition-colors flex flex-col lg:flex-row lg:items-center justify-between gap-4">' +
          '<div class="flex items-start gap-3.5 flex-1 min-w-0">' +
          '<span class="w-2 h-2 rounded-full ' + dot + ' mt-1.5 shrink-0"></span>' +
          '<div class="min-w-0">' +
          '<div class="font-headline-sm text-body-md text-on-surface">' + D().esc(a.title) + '</div>' +
          '<div class="font-body-sm text-body-sm text-on-surface-variant truncate">' + D().esc(a.explanation || '') + '</div>' +
          '<div class="font-data-mono-sm text-data-mono-sm text-tertiary mt-0.5">' + D().esc(String(a.ruleCode || '')) + ' · ' + D().fmtDate(a.detectedAt) + '</div>' +
          '</div></div>' +
          '<div class="flex items-center gap-3">' + D().badge(sev) + D().badge(a.status) + '</div>' +
          '</div>';
      }).join('');
      if (rows) { rowsRoot.innerHTML = rows; }
    }).catch(function () {
      D().notice('Backend unreachable — showing demo data.');
    });
  }

  // ------------------------------------------------------------------
  // initiate new verification: dispatch button creates a REAL verification
  // ------------------------------------------------------------------
  function wireInitiate() {
    var dispatchBtn = document.getElementById('dispatchVerificationBtn');
    var modal = document.getElementById('dispatchSuccessModal');
    var modalName = document.getElementById('modalCandidateName');
    var modalEmp = document.getElementById('modalEmployer');
    var modalTrack = document.getElementById('modalTrackBtn');
    if (!dispatchBtn || dispatchBtn.dataset.twcWired) { return; }
    dispatchBtn.dataset.twcWired = '1';

    dispatchBtn.addEventListener('click', function (ev) {
      ev.preventDefault();
      var firstName = (document.getElementById('candidateFirstName') || {}).value || '';
      var lastName = (document.getElementById('candidateLastName') || {}).value || '';
      var name = (firstName + ' ' + lastName).trim() || 'Demo Candidate';
      var employer = (document.getElementById('employerSearchInput') || {}).value || '';
      var empId = (document.getElementById('employeeId') || {}).value || '';
      var purpose = (document.getElementById('fcraPurposeSelect') || {}).value || 'LOAN';
      var purposeMap = { loan: 'LOAN', mortgage: 'MORTGAGE', employment: 'EMPLOYMENT_SCREENING', housing: 'HOUSING', benefits: 'BENEFITS', security: 'OTHER' };

      // Block dispatch without the FCRA consent checkbox when present.
      var consent = document.getElementById('fcraConsentCheck');
      if (consent && consent.type === 'checkbox' && !consent.checked) {
        TWC.showToast('FCRA authorization is required before dispatch.', 'error');
        return;
      }

      var original = dispatchBtn.innerHTML;
      dispatchBtn.disabled = true;
      dispatchBtn.innerHTML = '<span class="material-symbols-outlined text-[18px] animate-spin">progress_activity</span><span>Dispatching…</span>';

      TWC.post('/api/v1/verifications', {
        employeeNumber: empId || 'EMP-49560',
        applicantName: name,
        employerName: employer || undefined,
        verificationType: 'EMPLOYMENT_AND_INCOME',
        purpose: purposeMap[purpose] || 'LOAN',
        lookbackMonths: 24,
        idempotencyKey: 'ui-' + Date.now()
      }, { silent: true }).then(function (data) {
        if (modalName) { modalName.textContent = name; }
        if (modalEmp) { modalEmp.textContent = employer || '—'; }
        if (modal) {
          var codeEl = document.getElementById('modalVerificationCode');
          if (codeEl) { codeEl.textContent = data.verificationCode || ''; }
          modal.classList.remove('hidden');
        }
        TWC.showToast('Verification ' + (data.verificationCode || '') + ' submitted — status ' + data.status, 'info');
        dispatchBtn.disabled = false;
        dispatchBtn.innerHTML = original;
      }).catch(function (err) {
        TWC.showToast(TWC.errorMessage ? TWC.errorMessage(err) : 'Dispatch failed.', 'error');
        dispatchBtn.disabled = false;
        dispatchBtn.innerHTML = original;
      });
    });

    // Track button navigates to the requests list where the new row appears.
    if (modalTrack) {
      modalTrack.addEventListener('click', function () {
        window.location.href = '/stitch-screens/verification_requests/code.html';
      });
    }
  }

  // ------------------------------------------------------------------
  // expose for manual use + run
  // ------------------------------------------------------------------
  global.TWC_BOOT = { rerun: wireScreenData };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})(window);
