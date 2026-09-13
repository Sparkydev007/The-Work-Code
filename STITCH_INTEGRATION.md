# STITCH_INTEGRATION

## Baseline preservation

The untouched Stitch screens were committed verbatim as the second commit
(`chore: preserve google stitch baseline`) before any backend work began. Visuals,
layout, typography (Inter + IBM Plex Sans), the Material Symbols set, and the
Verified Ledger design system (`frontend/stitch-screens/verified_ledger/DESIGN.md`)
are preserved.

## Screen → backend map

| Stitch screen (frontend/stitch-screens/…) | Backend endpoints |
|---|---|
| `main_operations_dashboard` | `GET /api/v1/analytics/dashboard`, `GET /api/v1/verifications?size=5`, `GET /api/health` |
| `sign_in_the_work_code` | `POST /api/v1/auth/login` (persona login; static fallback otherwise) |
| `verification_requests` | `GET /api/v1/verifications`, `POST /api/v1/verifications`, `POST …/{id}/rerun` |
| `verification_detail_dossier_ver_100293` | `GET /api/v1/verifications/{id}` (workflow steps, risk, refs) |
| `initiate_new_verification` | `POST /api/v1/verifications` (+ `?simulate=` failure injection) |
| `employees_directory` | `GET /api/v1/employees` (search/filter/paging/masking) |
| `income_verification` | `GET /api/v1/income/employees/{id}/summary · pay-periods · trend`, `POST /api/v1/income/verifications` |
| `risk_fraud_analytics` | `GET /api/v1/risk/anomalies`, `GET /api/v1/risk/{verificationId}` |
| `anomalies` | `GET /api/v1/risk/anomalies` (+ summary) |
| `verification_reports` | `GET /api/v1/reports`, `…/download`, `…/regenerate` |
| `api_keys_logs` | Gateway contract ready (API-key service is roadmap) |
| `webhooks` | `GET/POST /api/v1/webhooks`, `…/test`, `…/deliveries`, `…/retry` |
| `audit_logs` | `GET /api/v1/audit?action=&actor=&resourceId=` |
| `security_center` | `GET /api/v1/verifications/credentialing/{orgId}`, org settings |
| `the_work_code_fully_interactive_operations_platform` | Composite sandbox screen (static) |
| `the_work_code_logo`, `…logo_guideline_sheet`, `…headshot` | Brand assets (static) |

## Added screens (Stitch-consistent, same design language)

| Screen | Purpose |
|---|---|
| `demo-interview` | Six scripted interview scenarios driving the live orchestrator |
| `api_docs` | Developer portal: all endpoints + curl example |
| `system_health` | Live aggregated service health from the gateway |

## Integration assets (non-destructive)

- `frontend/assets/js/twc-api.js` — central client: envelope unwrap, persona login,
  `USE_DEMO_DATA` fallback so screens never blank out, demo-mode banner injection.
- `frontend/assets/js/twc-demo-launcher.js` — scenario runner with animated steps.
- `frontend/index.html` — routes to the sign-in screen.
- `frontend/nginx.conf` — same-origin `/api` proxy (kills CORS issues in Docker).

Wiring an existing screen to live data is additive: include `twc-api.js` and replace a
hardcoded array with `TWC.get('/api/v1/…')` inside the screen's existing render path.
