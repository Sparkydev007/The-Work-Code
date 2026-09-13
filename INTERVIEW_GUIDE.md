# INTERVIEW GUIDE

How to demo and defend The Work Code in 5, 15 and 45 minutes.

## Elevator pitch (30s)

"An enterprise-style employment and income verification platform: a Java Spring Boot
microservices backend behind an API gateway, PostgreSQL with per-service schemas, a
preserved Google Stitch frontend, deterministic identity matching and risk engines, and
a full demo dataset — all free-tier deployable. Everything is synthetic; no commercial
verification network is connected."

## The 5-minute demo path

1. `docker compose up --build` → open http://localhost:3000 (sign-in screen).
2. Persona login → **Admin**. Note the DEMO MODE badge.
3. New Verification → Rahul Sharma / DEMO-PERFECT-001 / Employment+Income / MORTGAGE.
4. Watch the workflow steps land in the verification dossier; result VERIFIED, LOW risk.
5. Risk & Fraud → seeded anomalies from the fraud scenario.
6. Audit Logs → your actions recorded with request IDs.

## The six scripted scenarios (interview launcher)

`frontend/stitch-screens/demo-interview/code.html`

| Scenario | Subject | What it proves |
|---|---|---|
| 1 Perfect | DEMO-PERFECT-001 | End-to-end happy path, MATCH/VERIFIED/LOW |
| 2 Mismatch | name vs record | Identity weighting → review zone |
| 3 No-hit | EMP-999999 | NO_HIT graceful outcome |
| 4 Income anomaly | DEMO-INCOME-001 | Variance rule → REVIEW + MEDIUM risk |
| 5 Duplicate | DEMO-FRAUD-001 | Duplicate/velocity/pattern rules → HIGH |
| 6 Manual | DEMO-MANUAL-001 | Fallback queue + simulated employer contact |

## Strongest technical talking points

1. **Orchestrator + state machine** — each verification step is a persisted workflow
   event; you can narrate exactly what ran, in order, with outcomes.
2. **Failure isolation** — kill income-service mid-demo: employment still verifies, final
   result degrades to REVIEW_REQUIRED. Then show `?simulate=INCOME` producing the same
   graceful path without touching infrastructure.
3. **Deterministic engines** — identity (40/25/25/10 weights, bigram-Dice + Jaccard +
   edit blend) and risk (10 rules, 5 dimensions, severity weighting) are pure functions
   with unit-locked behavior; explainable scores with recommended actions.
4. **Idempotency** — `Idempotency-Key` returns the original verification instead of a
   duplicate; talk about safe retries in payment/verification APIs.
5. **Provider abstraction** — `IdentityVerificationProvider` etc.: swap the demo source
   for a production provider without touching orchestration.
6. **Data integrity** — synthetic generator enforces hire/termination, tenure
   non-overlap, pay-frequency math, annualization; locked by unit tests.
7. **Security seams** — gateway JWT + server-side RoleGuard, correlation IDs end-to-end,
   masking, append-only audit; honest about what is demo vs production-shaped.
8. **Trade-offs** — schema-per-service on one cluster, synchronous orchestration with
   queue-ready seams, simulated webhooks: each is a deliberate free-tier decision with a
   documented production evolution.

## Likely deep-dive questions

- **Why microservices for a demo?** Boundaries mirror real ownership/scaling/compliance
  splits; also lets me show distributed concerns: correlation IDs, timeouts, partial
  failure, health aggregation.
- **Why synchronous orchestration?** Free-tier operability; the workflow-event log and
  provider interfaces make a Kafka/Pub-Sub backbone a drop-in upgrade — documented in
  ARCHITECTURE.md.
- **How would you scale identity matching?** Pure functions → stateless horizontal
  scaling; candidate resolution is one indexed employee search; cache candidate lookups.
- **What breaks first in production?** The single Postgres cluster; move hot services
  (identity lookups, pay periods) to dedicated instances with read replicas.
- **How is PII handled?** No real PII exists. Masking is default-on for lists; DOB and
  income are redacted from masked views; audit records reference, never contain, values.
