#!/usr/bin/env bash
# Launch all services and keep them alive. Run via a persistent process.
cd "$(dirname "$0")/.."

export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5434/workcode}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-workcode}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-workcode}"
export JWT_SECRET="${JWT_SECRET:-demo-secret-key-change-me-0123456789abcdef}"
export SEED_DEMO_DATA="${SEED_DEMO_DATA:-true}"

mkdir -p target/logs

for svc in identity-service employee-service employer-service employment-service income-service risk-service report-service audit-service batch-service notification-service verification-service; do
  nohup java -jar "services/$svc/target/"*.jar > "target/logs/$svc.log" 2>&1 &
  echo "$!" >> target/logs/pids.txt
done
nohup java -jar services/api-gateway/target/*.jar > target/logs/api-gateway.log 2>&1 &
echo "$!" >> target/logs/pids.txt

echo "all launched; pids in target/logs/pids.txt"
wait
