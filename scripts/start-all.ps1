# Restart the full local stack (all services + gateway) after a common-lib rebuild.
$ErrorActionPreference = "Stop"
$env:SPRING_DATASOURCE_URL  = "jdbc:postgresql://localhost:5434/workcode"
$env:SPRING_DATASOURCE_USERNAME = "workcode"
$env:SPRING_DATASOURCE_PASSWORD = "workcode"
$env:JWT_SECRET = "demo-secret-key-change-me-0123456789abcdef"
$env:SEED_DEMO_DATA = "false"

$root = Split-Path -Parent $PSScriptRoot

$services = @(
  "identity-service", "employee-service", "employer-service",
  "employment-service", "income-service", "verification-service",
  "risk-service", "report-service", "batch-service",
  "audit-service", "notification-service", "api-gateway"
)

foreach ($svc in $services) {
  $jar = Join-Path $root "services\$svc\target\$svc-0.1.0-SNAPSHOT.jar"
  Start-Process -FilePath "java" `
    -ArgumentList @("-Xmx256m", "-jar", "`"$jar`"") `
    -WorkingDirectory $root `
    -WindowStyle Hidden `
    -RedirectStandardOutput (Join-Path $root "target\logs\$svc.log") `
    -RedirectStandardError  (Join-Path $root "target\logs\$svc-err.log")
  Start-Sleep -Milliseconds 400
  Write-Output "LAUNCHED $svc"
}
