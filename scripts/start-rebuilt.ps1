# Launch rebuilt services (employee, employer, audit, verification)
$ErrorActionPreference = "Stop"
$env:SPRING_DATASOURCE_URL  = "jdbc:postgresql://localhost:5434/workcode"
$env:SPRING_DATASOURCE_USERNAME = "workcode"
$env:SPRING_DATASOURCE_PASSWORD = "workcode"
$env:JWT_SECRET = "demo-secret-key-change-me-0123456789abcdef"
$env:SEED_DEMO_DATA = "false"

$root = Split-Path -Parent $PSScriptRoot

$services = @("employee-service", "employer-service", "audit-service", "verification-service")

foreach ($svc in $services) {
  $jar = Join-Path $root "services\$svc\target\$svc-0.1.0-SNAPSHOT.jar"
  Start-Process -FilePath "java" `
    -ArgumentList @("-Xmx256m", "-jar", "`"$jar`"") `
    -WorkingDirectory $root `
    -WindowStyle Hidden `
    -RedirectStandardOutput (Join-Path $root "target\logs\$svc.log") `
    -RedirectStandardError  (Join-Path $root "target\logs\$svc-err.log")
  Write-Output "LAUNCHED $svc"
}
