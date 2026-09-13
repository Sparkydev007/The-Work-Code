#!/usr/bin/env bash
# The Work Code - local development launcher (without Docker for services).
# Prereq: Postgres running locally with db 'workcode' user/pass 'workcode'.
# Usage: ./scripts/dev-up.sh
set -euo pipefail

cd "$(dirname "$0")/.."

echo "== Building all modules =="
mvn -q package -DskipTests

echo "== Starting services (background, logs in target/logs) =="
mkdir -p target/logs

declare -A SERVICES=(
  [identity-service]=8081
  [employee-service]=8082
  [employer-service]=8083
  [employment-service]=8084
  [income-service]=8085
  [verification-service]=8086
  [risk-service]=8087
  [report-service]=8088
  [audit-service]=8089
  [batch-service]=8090
  [notification-service]=8091
)

for svc in "${!SERVICES[@]}"; do
  port="${SERVICES[$svc]}"
  echo "  starting $svc on :$port"
  java -jar "services/$svc/target/"*.jar > "target/logs/$svc.log" 2>&1 &
done

echo "== Starting gateway on :8080 =="
java -jar services/api-gateway/target/*.jar > target/logs/api-gateway.log 2>&1 &

echo "== Serving frontend at :3000 =="
cd frontend
if command -v python >/dev/null 2>&1; then
  python -m http.server 3000 >/dev/null 2>&1 &
elif command -v npx >/dev/null 2>&1; then
  npx --yes serve -l 3000 . >/dev/null 2>&1 &
fi

echo
echo "UI:      http://localhost:3000/stitch-screens/sign_in_the_work_code/code.html"
echo "Gateway: http://localhost:8080/api/health"
echo "Logs:    target/logs/"
