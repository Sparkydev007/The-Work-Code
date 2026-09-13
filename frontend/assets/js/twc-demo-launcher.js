/**
 * The Work Code - Interview Demo Launcher.
 * One-click scenarios (spec #80). Each scenario drives the real backend
 * through the gateway when available and falls back to narrated demo mode
 * otherwise, so interviews never stall.
 *
 * Include after twc-api.js:
 *   <script src="/assets/js/twc-api.js"></script>
 *   <script src="/assets/js/twc-demo-launcher.js"></script>
 * Then call TWC_DEMO.mount(containerElement).
 */
(function (global) {
  'use strict';

  var SCENARIOS = [
    {
      key: 'perfect',
      title: 'Scenario 1 — Perfect Employee Verification',
      description: 'Rahul Sharma (DEMO-PERFECT-001) at Acme Technologies. Expect MATCH identity, VERIFIED employment + income, LOW risk.',
      request: {
        verificationType: 'EMPLOYMENT_AND_INCOME',
        purpose: 'MORTGAGE',
        applicantName: 'Rahul Sharma',
        applicantEmail: 'rahul.sharma@acme-demo.com',
        employeeNumber: 'DEMO-PERFECT-001',
        employerName: 'Acme Technologies Pvt Ltd',
        lookbackMonths: 24,
        idempotencyKey: 'demo-perfect-001'
      }
    },
    {
      key: 'mismatch',
      title: 'Scenario 2 — Identity Mismatch',
      description: 'Applicant name does not match the record on file. Expect NO_MATCH identity and REVIEW routing.',
      request: {
        verificationType: 'EMPLOYMENT',
        purpose: 'LOAN',
        applicantName: 'Vikram Desai',
        employeeNumber: 'DEMO-PERFECT-001',
        employerName: 'Acme Technologies Pvt Ltd',
        idempotencyKey: 'demo-mismatch-001'
      }
    },
    {
      key: 'nohit',
      title: 'Scenario 3 — No Hit',
      description: 'Applicant does not exist in the synthetic index. Expect NO_HIT with a graceful outcome.',
      request: {
        verificationType: 'EMPLOYMENT',
        purpose: 'BENEFITS',
        applicantName: 'Nonexistent Person',
        employeeNumber: 'EMP-999999',
        employerName: 'Nowhere Corp',
        idempotencyKey: 'demo-nohit-001'
      }
    },
    {
      key: 'income',
      title: 'Scenario 4 — Income Anomaly',
      description: 'Payroll variance above threshold for Priya Nair (DEMO-INCOME-001). Expect income REVIEW and risk MEDIUM+.',
      request: {
        verificationType: 'INCOME',
        purpose: 'AUTO_FINANCE',
        applicantName: 'Priya Nair',
        employeeNumber: 'DEMO-INCOME-001',
        employerName: 'BluePeak Financial',
        lookbackMonths: 12,
        idempotencyKey: 'demo-income-001'
      }
    },
    {
      key: 'fraud',
      title: 'Scenario 5 — Duplicate Identity / Fraud Signals',
      description: 'Vikram Rao (DEMO-FRAUD-001) trips duplicate, velocity and pattern heuristics. Expect HIGH/CRITICAL risk.',
      request: {
        verificationType: 'EMPLOYMENT_AND_INCOME',
        purpose: 'HOUSING',
        applicantName: 'Vikram Rao',
        employeeNumber: 'DEMO-FRAUD-001',
        employerName: 'Trident Fintech',
        idempotencyKey: 'demo-fraud-001'
      }
    },
    {
      key: 'manual',
      title: 'Scenario 6 — Manual Verification Fallback',
      description: 'Farhan Ali (DEMO-MANUAL-001) at a low-coverage employer. Case lands in the manual queue for simulated employer contact.',
      request: {
        verificationType: 'EMPLOYMENT_AND_INCOME',
        purpose: 'EMPLOYMENT_SCREENING',
        applicantName: 'Farhan Ali',
        employeeNumber: 'DEMO-MANUAL-001',
        employerName: 'Kite Education Group',
        idempotencyKey: 'demo-manual-001'
      }
    }
  ];

  function el(tag, cls, text) {
    var e = document.createElement(tag);
    if (cls) { e.className = cls; }
    if (text !== undefined) { e.textContent = text; }
    return e;
  }

  function logLine(logBox, text, kind) {
    var line = el('div', 'twc-demo-log-line' + (kind ? ' twc-demo-' + kind : ''), text);
    logBox.appendChild(line);
    logBox.scrollTop = logBox.scrollHeight;
  }

  function runScenario(scenario, logBox, onDone) {
    logBox.innerHTML = '';
    logLine(logBox, '▶ ' + scenario.title, 'title');
    logLine(logBox, scenario.description);

    var steps = ['Request Submitted', 'Identity Matching', 'Employer Resolution', 'Employment Lookup',
      'Income Lookup', 'Risk Analysis', 'Report Generation'];
    var i = 0;
    var timer = setInterval(function () {
      if (i < steps.length) {
        logLine(logBox, '✓ ' + steps[i]);
        i++;
        return;
      }
      clearInterval(timer);
      global.TWC.post('/api/v1/verifications', scenario.request)
        .then(function (data) {
          logLine(logBox, 'Backend result: status=' + data.status + ' code=' + data.verificationCode, 'ok');
          logLine(logBox, 'Open Verification Requests to inspect the full dossier.', 'ok');
          if (onDone) { onDone(data); }
        })
        .catch(function (err) {
          logLine(logBox, 'Backend unavailable (' + global.TWC.errorMessage(err) + ') — showing demo outcome.', 'warn');
          var outcomes = {
            perfect: 'VERIFIED · MATCH · Risk 14 (LOW) · Report RPT-DEMO-1',
            mismatch: 'VERIFIED WITH REVIEW · NO_MATCH (61%) · Risk 46 (MEDIUM)',
            nohit: 'NOT VERIFIED · NO_HIT · manual verification suggested',
            income: 'VERIFIED WITH REVIEW · income variance 62% · Risk 52 (MEDIUM)',
            fraud: 'VERIFIED WITH REVIEW · DUPLICATE_IDENTITY · Risk 78 (HIGH)',
            manual: 'REVIEW_REQUIRED · routed to manual verification queue'
          };
          logLine(logBox, 'Demo outcome: ' + (outcomes[scenario.key] || 'processed'), 'ok');
          if (onDone) { onDone(null); }
        });
    }, 260);
  }

  function mount(container) {
    if (typeof container === 'string') {
      container = document.querySelector(container);
    }
    if (!container) { return; }

    var wrapper = el('div', 'twc-demo-launcher');
    wrapper.style.cssText = 'background:#fff;border:1px solid #e2e8f0;border-radius:8px;padding:20px;';

    wrapper.appendChild(el('h2', null, 'Interview Demo Launcher'));
    wrapper.appendChild(el('p', 'twc-demo-sub',
      'One-click scenarios against the live orchestrator (falls back to narrated demo mode offline).'));

    var grid = el('div');
    grid.style.cssText = 'display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:12px;margin-top:14px;';

    var logBox = el('div', 'twc-demo-log');
    logBox.style.cssText = 'margin-top:14px;background:#0b192c;color:#cbd5e1;font:12px/1.6 IBM Plex Sans,monospace;' +
      'padding:14px;border-radius:6px;min-height:120px;max-height:280px;overflow:auto;display:none;';

    SCENARIOS.forEach(function (scenario) {
      var card = el('button', 'twc-demo-card');
      card.style.cssText = 'text-align:left;background:#f8fafc;border:1px solid #e2e8f0;border-radius:6px;' +
        'padding:12px;cursor:pointer;transition:all .15s;';
      card.onmouseenter = function () { card.style.background = '#eff6ff'; card.style.borderColor = '#2563eb'; };
      card.onmouseleave = function () { card.style.background = '#f8fafc'; card.style.borderColor = '#e2e8f0'; };
      card.appendChild(el('div', 'twc-demo-card-title', scenario.title));
      card.querySelector('.twc-demo-card-title').style.cssText =
        'font:600 13px/1.4 Inter,sans-serif;color:#0b192c;margin-bottom:6px;';
      card.appendChild(el('div', 'twc-demo-card-desc', scenario.description));
      card.querySelector('.twc-demo-card-desc').style.cssText =
        'font:12px/1.5 Inter,sans-serif;color:#64748b;';
      card.onclick = function () {
        logBox.style.display = 'block';
        runScenario(scenario, logBox);
      };
      grid.appendChild(card);
    });

    wrapper.appendChild(grid);
    wrapper.appendChild(logBox);
    container.appendChild(wrapper);
  }

  global.TWC_DEMO = { mount: mount, SCENARIOS: SCENARIOS, run: runScenario };
})(window);
