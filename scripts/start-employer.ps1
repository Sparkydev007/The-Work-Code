# Launch employer-service with correct path quoting
$env:SPRING_DATASOURCE_URL  = "jdbc:postgresql://localhost:5434/workcode"
$env:SPRING_DATASOURCE_USERNAME = "workcode"
$env:SPRING_DATASOURCE_PASSWORD = "workcode"
$env:JWT_SECRET = "demo-secret-key-change-me-0123456789abcdef"
$env:SEED_DEMO_DATA = "true"

$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root "services\employer-service\target\employer-service-0.1.0-SNAPSHOT.jar"

Start-Process -FilePath "java" `
  -ArgumentList @("-jar", "`"$jar`"") `
  -WorkingDirectory $root `
  -WindowStyle Hidden `
  -RedirectStandardOutput (Join-Path $root "target\logs\employer-service.log") `
  -RedirectStandardError  (Join-Path $root "target\logs\employer-service-err.log")

Write-Output "EMPLOYER-LAUNCHED"
