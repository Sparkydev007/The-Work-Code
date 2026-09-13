# DATABASE

PostgreSQL with **schema-per-service** on one cluster (free-tier compromise; production
should use independently managed databases where isolation requirements justify it).

| Schema | Owner service | Key tables |
|---|---|---|
| `identity-service` | identity | identity_verifications |
| `employee-service` | employee | employees |
| `employer-service` | employer | employers |
| `employment-service` | employment | employment_records, employment_verifications |
| `income-service` | income | income_records, pay_periods, income_verifications |
| `verification-service` | verification | verification_requests, verification_workflow_events, disputes, manual_verifications, organization_credentialing |
| `risk-service` | risk | risk_assessments, risk_anomalies |
| `report-service` | report | reports (incl. PDF bytea) |
| `audit-service` | audit | audit_events |
| `batch-service` | batch | batch_jobs, batch_items |
| `notification-service` | notification | webhooks, webhook_deliveries, notifications |

## Conventions

- UUID primary keys (`GenerationType.UUID`).
- `created_at` / `updated_at` on mutable aggregates; immutable-style audit table has no update path.
- Flyway migrations per service (`classpath:db/migration`), `ddl-auto=validate`.
- `create-schemas: true` lets each service provision its own schema on boot.
- Indexes on every foreign-shaped lookup column: employee_number, employer_code,
  status, verification_id, pay_date, created_at, email.

## Migrations

Each service ships `V1__init.sql`. Flyway applies on startup inside the service schema:

```text
services/<name>/src/main/resources/db/migration/V1__init.sql
```

## Pointing at Neon / Supabase

Set the standard Spring properties (see `.env.example`):

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
```

All services read the same variables; in docker compose override them once in the
`x-service-base` environment block or via `env_file: .env`.
