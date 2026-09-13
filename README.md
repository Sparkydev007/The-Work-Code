# The Work Code — Employment & Income Verification Platform

> Enterprise-style employment, income and identity verification infrastructure built as an
> interview-grade portfolio project. **This project uses synthetic demonstration data only and
> does not connect to Equifax or The Work Number.** It is not affiliated with any commercial
> verification bureau.

Java 21 target (runs on 17+) · Spring Boot 3.3 · 12 microservices · PostgreSQL · Flyway ·
JWT + RBAC · Google Stitch frontend (preserved) · Docker Compose · free-tier deployable.

## Quick start

```bash
# 1) Full stack (recommended)
cp .env.example .env         # optional; defaults work
docker compose up --build    # postgres + 11 services + gateway + UI

# 2) Open the platform
#    UI:      http://localhost:3000  (routes to the Stitch sign-in screen)
#    Gateway: http://localhost:8080
#    Health:  http://localhost:8080/api/health
```

### Demo personas (one-click login, no passwords)

| Persona   | Username   | Role      | Notes                          |
|-----------|------------|-----------|--------------------------------|
| Admin     | `admin`    | ADMIN     | Everything                     |
| Analyst   | `analyst`  | ANALYST   | Verifications, risk, reports   |
| Developer | `developer`| DEVELOPER | API keys, webhooks, logs       |
| Viewer    | `viewer`   | VIEWER    | Read-only                      |

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" -d '{"username":"admin"}'
```

### Interview demo

`frontend/stitch-screens/demo-interview/code.html` — six scripted scenarios
(perfect match, mismatch, no-hit, income anomaly, duplicate identity, manual fallback)
that drive the real orchestrator end to end.

## Feature matrix

| Feature                    | Demo | | Feature                    | Demo |
|----------------------------|------|-|----------------------------|------|
| Employment Verification    | YES  | | Batch Processing           | YES  |
| Income Verification        | YES  | | PDF Reports                | YES  |
| Identity Match             | YES  | | Disputes                   | YES  |
| MATCH / NO_MATCH / NO_HIT  | YES  | | Audit Logs (immutable)     | YES  |
| Historical Employment      | YES  | | RBAC                       | YES  |
| 12/24/36-month Lookback    | YES  | | API Gateway + JWT          | YES  |
| Pay Period Data            | YES  | | Webhooks (simulated)       | YES  |
| Risk Signals (10 rules)    | YES  | | Manual Verification        | YES  |
| Fraud/Anomaly Detection    | YES  | | Employee Self-Service data | YES  |
| Analytics                  | YES  | | System Health              | YES  |

## Architecture (demo runtime)

```text
Stitch UI (nginx :3000)
      │  /api (same-origin proxy)
      ▼
API Gateway (:8080) ── JWT auth · demo personas · rate limit · X-Request-ID · /api/health
      │
      ├─ identity-service    (:8081)  matching engine: MATCH / NO_MATCH / NO_HIT
      ├─ employee-service    (:8082)  synthetic directory (500 employees) + seeding
      ├─ employer-service    (:8083)  employer network + resilient cross-service profile
      ├─ employment-service  (:8084)  VOE + history timeline
      ├─ income-service      (:8085)  VOI + pay periods + lookback + trends
      ├─ verification-service(:8086)  orchestrator, state machine, disputes, manual queue, analytics
      ├─ risk-service        (:8087)  10-rule risk engine + anomaly store
      ├─ report-service      (:8088)  OpenPDF reports
      ├─ audit-service       (:8089)  append-only audit trail
      ├─ batch-service       (:8090)  CSV batch pipeline
      └─ notification-service(:8091)  webhooks + notifications (simulated delivery)
      │
      ▼
PostgreSQL (per-service schemas, Flyway migrations)
```

See `ARCHITECTURE.md` for the production evolution (Kubernetes, Kafka, per-service DBs).

## Verification workflow

```text
POST /api/v1/verifications
  → CREATED → PROCESSING
  → IDENTITY_MATCHING      (identity-service, weighted scoring)
  → EMPLOYMENT_LOOKUP      (employment-service, VOE)
  → INCOME_LOOKUP          (income-service, VOI)     [income/combined types]
  → RISK_ANALYSIS          (risk-service, 10 rules)
  → REPORT_GENERATION      (report-service, PDF)
  → COMPLETED | REVIEW_REQUIRED | FAILED
```

Every step is persisted as a workflow event (observable in the verification dossier),
every downstream call is failure-isolated, and `?simulate=IDENTITY|EMPLOYMENT|INCOME`
injects demo failures for resilience demos.

## Identity matching algorithm (original, documented)

Weights: **name 40% · employer 25% · employee ID 25% · email/domain 10%**.
Similarity = character-bigram Dice + token Jaccard + edit-distance blend.
Missing attributes score neutral (0.5). Thresholds: ≥90 MATCH · 60–89 review zone ·
<60 NO_MATCH · no candidate record → NO_HIT.
This is an original demo algorithm, **not** derived from any commercial provider.

## Risk engine (original, documented)

Ten transparent rules across five dimensions (identity 30, employment 25, income 20,
request 15, duplicate 10) with severity-weighted scoring: 0–29 LOW · 30–59 MEDIUM ·
60–79 HIGH · 80–100 CRITICAL. Every score carries human-readable explanations and
recommended actions; HIGH+ signals raise persisted anomalies.

## Synthetic data

Deterministic (seeded) generator produces: 20 employers, 500 employees, ~1,100
employment records, thousands of pay periods aligned to hire dates and pay frequencies,
plus fixed scenario subjects: `DEMO-PERFECT-001`, `DEMO-MISMATCH-001`, `DEMO-INCOME-001`,
`DEMO-FRAUD-001`, `DEMO-MANUAL-001`. All people and companies are fictional.

## Repository layout

```text
frontend/            Stitch UI (preserved) + integration assets + new screens
services/            11 domain services + api-gateway
shared/common-lib/   API envelope, error codes, JWT, correlation, demo data factory
scripts/             dev helpers
docs/                diagrams and screenshots
docker-compose.yml   full local stack
```

## Commands

```bash
mvn clean verify          # build + all unit tests
docker compose up --build # full stack
./scripts/dev-up.sh       # local (host) run without Docker for services
```

## Docs

`ARCHITECTURE.md` · `API.md` · `DATABASE.md` · `SECURITY.md` · `DEPLOYMENT.md` ·
`TESTING.md` · `INTERVIEW_GUIDE.md` · `STITCH_INTEGRATION.md` · `PROJECT_PROGRESS.md`

## Limitations (honest)

- Demo/simulated verification providers only; no external network connectivity.
- Webhook delivery is simulated (deterministic outcomes), not real HTTP.
- Orchestrator is synchronous in-process; production should queue (documented).
- API key management UI is roadmap (schema + gateway contract ready).
- Analytics type-distribution is simplified (counts per type placeholder).

## License / attribution note

Original portfolio implementation. Product concepts are inspired by the public
capabilities of workforce-verification platforms; no proprietary code, data,
trademarks, logos or UI assets from any commercial provider are included.
