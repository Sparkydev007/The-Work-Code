# PROJECT_PROGRESS

| Phase | Scope | Status |
|-------|-------|--------|
| 0 | Repository inspection, Stitch baseline commit (`chore: preserve google stitch baseline`) | COMPLETE |
| 1 | Parent POM, common-lib (envelope, errors, JWT, correlation, demo data factory) | COMPLETE |
| 2 | Database strategy: per-service schemas, Flyway; local Postgres now / Neon handoff | COMPLETE |
| 3 | employee-service, employer-service (search, masking, seeding, resilient call) | COMPLETE |
| 4 | identity-service (matching engine + 8 tests) | COMPLETE |
| 5 | employment-service (VOE, history) | COMPLETE |
| 6 | income-service (VOI, pay periods, lookback, trends) | COMPLETE |
| 7 | verification-service (orchestrator, state machine, disputes, manual queue, credentialing, analytics) | COMPLETE |
| 8 | risk-service (10 rules, scoring, anomalies + 7 tests) | COMPLETE |
| 9 | report-service (OpenPDF, download/regenerate) | COMPLETE |
| 10 | batch-service (CSV validate/process/export) | COMPLETE |
| 11 | audit-service (append-only + seeded events) | COMPLETE |
| 12 | notification-service (webhooks, simulated delivery, retry) | COMPLETE |
| 13 | api-gateway (routes, JWT persona login, rate limit, health aggregation) | COMPLETE |
| 14 | Frontend: central API client, demo launcher, health + API docs screens, demo banner | COMPLETE |
| 15 | Tests: 25 unit tests green (common-lib, identity, risk) | COMPLETE |
| 16 | Dockerfiles + docker-compose full stack | COMPLETE |
| 17 | Documentation set | COMPLETE |
| 18 | Final verification + Neon/Supabase handoff | COMPLETE |

## Bugs found & fixed during build

1. Multi-module `relativePath` — child POMs two levels deep needed `../../pom.xml`.
2. `DemoDataFactory` — `List.get` vs array indexing; duplicate local variable names in pay-period loop; duplicate switch case labels.
3. `JwtSupport` — wrong charset (`UTF_32` → `UTF_8`) for HMAC key bytes.
4. `HealthAggregator` — Reactor type inference failures; fixed with an explicit `ServiceStatus` record.
5. Identity engine test failures exposed weak Jaccard-only similarity ("Sharman" vs "Sharma"); replaced with bigram-Dice + Jaccard + edit blend, punctuation normalization fixed.
6. Risk engine initial weights too gentle — single critical signal couldn't reach HIGH; added ×2 amplifier with saturation cap; band tests rewritten.
7. Spring `RestClient` API misuse (`bodyToMono` on sync client, `record.get(key, default)` in Commons CSV) — corrected.
8. Two failed `write_file` transfers produced corrupted sources mid-build; both detected by compile and rewritten.

## Remaining issues / roadmap

- Playwright E2E suite is specced in TESTING.md but not yet implemented.
- API-key management service/UI pending (gateway contract documented).
- Orchestrator processes synchronously; event-queue backend documented for production.
- Analytics type-distribution uses placeholder counts; wire per-type group-bys next.
