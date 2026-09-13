# SECURITY

Demo-grade security with production-shaped seams. **No real personal data is processed
anywhere in this project.**

## Implemented

- **JWT (HS256)** issued at the gateway for four demo personas; validated on every
  protected request; tampered/expired/garbage tokens rejected (unit-tested).
- **RBAC server-side only.** The gateway forwards authenticated identity headers;
  every service re-checks capability via `RoleGuard` (admin/analyst/developer/viewer).
  Frontend role state is never trusted.
- **Correlation IDs**: `X-Request-ID` generated/propagated at the gateway, echoed on
  responses, logged everywhere — full request traceability.
- **Input validation** with Bean Validation on all POST/PUT bodies; failures return 400
  with field-level details in the standard envelope.
- **Data masking** (`?masked=true` / list default): DOB and income hidden, emails/phones
  partially masked (`r••••@…`, `•••••4821`).
- **Rate limiting** at the gateway (per-IP window, 429 with `RATE_LIMITED`).
- **Append-only audit log** for sensitive actions (login, create/view/rerun verification,
  download report, create/revoke API key, submit/update dispute, export batch).
- **No secrets in the repo**: `.env.example` only; JWT secret injected via environment.
- **CORS** restricted to local dev origins; never `*` for credentialed routes.
- **Non-root containers**, layered jar caching, minimal JRE runtime images.

## Demo-mode distinctions (honest)

- Personas are passwordless demo logins; production needs real accounts, hashed
  credentials (BCrypt/Argon2), refresh tokens and revocation.
- API keys are designed (hash+prefix storage, create/revoke/rotate contract) but the
  management UI/service is roadmap.
- CSRF is not applicable to the pure token API; the UI uses no cookies for auth.

## Production checklist (documented, not implemented)

Secrets manager · token rotation · per-service mTLS or signed service tokens ·
distributed rate limiting · request-body size limits · dependency scanning in CI ·
immutable log shipping · PII encryption at rest with per-tenant keys.
