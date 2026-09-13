# TESTING

## Current suites (25 tests, all green)

| Module | Suite | Covers |
|---|---|---|
| common-lib | `DemoDataFactoryTest` (9) | Deterministic seeds, unique employee numbers, scenario subjects, hire/termination consistency, non-overlapping tenures, pay-period math (gross = base+OT+bonus+comm; net = gross−deductions), annualization |
| common-lib | `JwtSupportTest` (5) | Issue/parse round trip, tamper rejection, garbage rejection, cross-secret rejection, RBAC capability checks |
| identity-service | `IdentityMatchingEngineTest` (8) | MATCH ≥90, typo tolerance, wrong-name → NO_MATCH, wrong-ID → NO_MATCH, unknown → NO_HIT, neutral scoring, determinism, normalization |
| risk-service | `RiskEngineTest` (7) | Clean file → LOW, duplicate/velocity/pattern → HIGH+, income anomaly rule, employer mismatch → MEDIUM, repeated failures escalation, band mapping, saturation at 100, determinism |

```bash
mvn clean verify        # runs everything
mvn -pl services/risk-service -am test   # single module
```

## Design notes

- Engines (identity, risk) are pure static functions over context maps — deliberately
  trivial to unit test without Spring.
- Data-consistency rules are enforced by the generator and locked by tests, so any
  regression in synthetic payroll realism fails CI.
- Controllers/services use constructor injection and thin DTO mapping, keeping the
  un-tested surface (wiring) minimal.

## Roadmap (documented, not yet implemented)

- **Testcontainers integration tests**: boot each service against real Postgres, run
  Flyway + seed, exercise repository queries and the orchestrator happy path.
- **Contract tests**: assert the shared envelope shape `{success, data, requestId}` on
  every controller via `@WebMvcTest`.
- **Playwright E2E**: login → dashboard → new verification → processing → result →
  report download → dispute → search; plus interview-launcher scenario smoke.
- **Failure injection tests**: `?simulate=IDENTITY|EMPLOYMENT|INCOME` producing
  REVIEW_REQUIRED / FAILED states and graceful UI.
