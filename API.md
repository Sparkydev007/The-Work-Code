# API REFERENCE

Base URL: `http://localhost:8080` (gateway). All responses use the standard envelope.

## Envelope

```json
{ "success": true, "data": { }, "requestId": "REQ-..." }
```

```json
{ "success": false,
  "error": { "code": "EMPLOYEE_NOT_FOUND", "message": "…", "recommendedAction": "…" },
  "requestId": "REQ-..." }
```

## Authentication

`POST /api/v1/auth/login` (public)

```json
{ "username": "admin" }        // admin | analyst | developer | viewer
```

→ `{ "token": "<jwt>", "tokenType": "Bearer", "expiresIn": 3600, "user": { … } }`

All other endpoints require `Authorization: Bearer <token>`.
The gateway forwards `X-Forwarded-User`, `X-Forwarded-User-Role`, `X-Forwarded-User-Name`;
services re-authorize server-side via `RoleGuard`.

## Endpoints by service

### Auth & health
| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/auth/login` | Demo persona login (public) |
| GET | `/api/health` | Aggregated service health (public) |

### Verifications (orchestrator)
| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/verifications` | Create + process. `?simulate=IDENTITY|EMPLOYMENT|INCOME` injects failures |
| GET | `/api/v1/verifications/{id}` | Full dossier incl. workflow steps |
| GET | `/api/v1/verifications` | `?status=&type=&search=&page=&size=` |
| POST | `/api/v1/verifications/{id}/rerun` | Re-run end to end |
| GET | `/api/v1/verifications/credentialing/{orgId}` | Credentialing state |
| POST | `/api/v1/verifications/credentialing/{orgId}/activate-demo` | Demo credentialing |
| GET | `/api/v1/analytics/dashboard` | Metrics + distributions |

### Identity
| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/identity/verify` | → MATCH / NO_MATCH / NO_HIT + attribute scores |
| GET | `/api/v1/identity/verifications/{id}` | Stored result |
| GET | `/api/v1/identity/verifications` | History (paged) |

### Employees / Employers
| Method | Path | Notes |
|---|---|---|
| GET | `/api/v1/employees` | `?search=&status=&employerCode=&department=&masked=&page=&size=` |
| GET | `/api/v1/employees/{employeeNumber}` | `?masked=true` for masking |
| POST / PUT | `/api/v1/employees[/{employeeNumber}]` | Create / update (write roles) |
| GET | `/api/v1/employers` | `?search=&industry=&trustStatus=` |
| GET | `/api/v1/employers/{code}/profile` | Profile + resilient employee count |

### Employment / Income
| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/employment/verifications` | VOE |
| GET | `/api/v1/employment/employees/{employeeNumber}/history` | Tenure timeline |
| POST | `/api/v1/income/verifications` | VOI (`lookbackMonths` 12/24/36) |
| GET | `/api/v1/income/employees/{employeeNumber}/summary` | Annual/monthly breakdown |
| GET | `/api/v1/income/employees/{employeeNumber}/pay-periods` | Pay period table |
| GET | `/api/v1/income/employees/{employeeNumber}/trend` | Trend points |

### Risk / Reports / Batch / Audit / Webhooks / Disputes / Manual
| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/risk/analyze` | Score + band + signals |
| GET | `/api/v1/risk/{verificationId}` | Latest assessment |
| GET | `/api/v1/risk/anomalies` | Anomaly feed (paged) |
| POST | `/api/v1/reports` | Generate report (PDF stored) |
| GET | `/api/v1/reports/{code}/download` | PDF download |
| POST | `/api/v1/reports/{code}/regenerate` | Regenerate |
| POST | `/api/v1/batches` | Multipart CSV upload |
| POST | `/api/v1/batches/{code}/submit` | Async demo processing |
| GET | `/api/v1/batches/{code}/results` · `/download` | Items / result CSV |
| GET | `/api/v1/audit` | `?action=&actor=&resourceId=` (paged) |
| POST | `/api/v1/webhooks` | Register webhook |
| POST | `/api/v1/webhooks/{id}/test` | `?simulateFailure=true` |
| POST | `/api/v1/webhooks/deliveries/{id}/retry` | Retry failed delivery |
| POST | `/api/v1/disputes` | Create dispute |
| POST | `/api/v1/disputes/{id}/status` | UNDER_REVIEW / RESOLVED / REJECTED |
| GET | `/api/v1/manual-verification` | Manual queue |
| POST | `/api/v1/manual-verification/{id}/simulate-employer-response` | Advance simulated contact |

## Error codes

`INVALID_REQUEST` 400 · `UNAUTHORIZED` 401 · `FORBIDDEN` 403 · `NOT_FOUND` 404 ·
`CONFLICT` 409 · `EMPLOYEE_NOT_FOUND` · `EMPLOYER_NOT_FOUND` · `VERIFICATION_NOT_FOUND` ·
`REPORT_NOT_FOUND` · `DISPUTE_NOT_FOUND` · `BATCH_NOT_FOUND` · `IDENTITY_NO_HIT` ·
`IDENTITY_NO_MATCH` 422 · `INSUFFICIENT_DATA` 422 · `CREDENTIALING_REQUIRED` 403 ·
`PROVIDER_UNAVAILABLE` 503 · `RATE_LIMITED` 429 · `INTERNAL_ERROR` 500.
