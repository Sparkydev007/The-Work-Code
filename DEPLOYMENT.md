# DEPLOYMENT

## Option A — local Docker (default demo)

```bash
docker compose up --build
# UI      http://localhost:3000
# Gateway http://localhost:8080
curl http://localhost:8080/api/health
```

## Option B — free-tier cloud

### 1. Database (Neon or Supabase)

```text
====================================================
HUMAN ACTION REQUIRED
====================================================

Action:
Create a free PostgreSQL database (Neon or Supabase).

Why:
The services need a managed Postgres connection string for cloud deployment.

Steps:
1. Open https://neon.tech (or https://supabase.com) and create a project.
2. Copy the connection string.
3. Convert to JDBC form:
   postgresql://user:pass@host/db?sslmode=require
   -> jdbc:postgresql://user:pass@host/db?sslmode=require
4. Keep it for the deployment step below.

After completing the action:
Return to the terminal and type:
DONE DATABASE
====================================================
```

### 2. Backend hosting (Render / Fly.io / Railway free tier)

The compose file is the source of truth. For a single-host demo deployment:

1. Create one service per container image (12 total), or one VM running
   `docker compose up -d` (the honest "single-host microservice demo deployment").
2. Provide env vars per service (see `.env.example`); set the Spring datasource
   properties from step 1 and a strong `JWT_SECRET` (32+ chars).
3. Health check path: `/actuator/health` (gateway also exposes `/api/health`).

### 3. Frontend (Vercel / Netlify / nginx)

- Deploy the `frontend/` folder as static hosting.
- Point the API proxy (`nginx.conf` style) or `TWC_API_BASE` at the deployed gateway.
- Keep CORS origins limited to the deployed frontend domain.

### 4. Post-deploy verification (smoke test)

```bash
BASE=https://your-gateway-host
curl $BASE/api/health
TOKEN=$(curl -s -X POST $BASE/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin"}' | jq -r .data.token)
curl -s -H "Authorization: Bearer $TOKEN" "$BASE/api/v1/employees?size=1" | head -c 400
curl -s -X POST $BASE/api/v1/verifications -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"verificationType":"EMPLOYMENT_AND_INCOME","purpose":"MORTGAGE","applicantName":"Rahul Sharma","employeeNumber":"DEMO-PERFECT-001","employerName":"Acme Technologies Pvt Ltd"}'
```

## Local development (no Docker for services)

```bash
# Prereq: local Postgres with db/user/pass = workcode/workcode/workcode
./scripts/dev-up.sh      # builds, starts 12 jars, serves UI on :3000
mvn clean verify         # build + tests
```
