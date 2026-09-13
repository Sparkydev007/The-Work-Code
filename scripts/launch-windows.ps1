# The Work Code - detached service launcher (Windows)
# Starts all 12 services as hidden, detached processes that survive the terminal.
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$env:SPRING_DATASOURCE_URL  = "jdbc:postgresql://localhost:5434/workcode"
$env:SPRING_DATASOURCE_USERNAME = "workcode"
$env:SPRING_DATASOURCE_PASSWORD = "workcode"
$env:JWT_SECRET = "demo-secret-key-change-me-0123456789abcdef"
$env:SEED_DEMO_DATA = "true"

$logDir = Join-Path $root "target\logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$services = @(
  "identity-service", "employee-service", "employer-service",
  "employment-service", "income-service", "risk-service",
  "report-service", "audit-service", "batch-service",
  "notification-service", "verification-service", "api-gateway"
)

foreach ($s in $services) {
  $jar = Get-ChildItem -Path (Join-Path $root "services\$s\target\*.jar") |
         Where-Object { $_.Name -notlike "*original*" } |
         Select-Object -First 1
  if (-not $jar) {
    Write-Warning "no jar found for $s"
    continue
  }
  Start-Process -FilePath "java" `
    -ArgumentList "-jar `"$($jar.FullName)`"" `
    -WorkingDirectory $root `
    -WindowStyle Hidden `
    -RedirectStandardOutput (Join-Path $logDir "$s.log") `
    -RedirectStandardError  (Join-Path $logDir "$s-err.log")
  Write-Output "launched $s"
}

Write-Output "ALL-12-LAUNCHED"
