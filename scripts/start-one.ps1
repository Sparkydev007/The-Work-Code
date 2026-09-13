# Restart a single rebuilt service by name, e.g. .\start-one.ps1 verification-service
param([string]$Svc = "verification-service")
$ErrorActionPreference = "Stop"
$env:SPRING_DATASOURCE_URL  = "jdbc:postgresql://localhost:5434/workcode"
$env:SPRING_DATASOURCE_USERNAME = "workcode"
$env:SPRING_DATASOURCE_PASSWORD = "workcode"
$env:JWT_SECRET = "demo-secret-key-change-me-0123456789abcdef"
$env:SEED_DEMO_DATA = "true"

$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root "services\$Svc\target\$Svc-0.1.0-SNAPSHOT.jar"
Start-Process -FilePath "java" `
  -ArgumentList @("-Xmx256m", "-jar", "`"$jar`"") `
  -WorkingDirectory $root `
  -WindowStyle Hidden `
  -RedirectStandardOutput (Join-Path $root "target\logs\$Svc.log") `
  -RedirectStandardError  (Join-Path $root "target\logs\$Svc-err.log")
Write-Output "LAUNCHED $Svc"
