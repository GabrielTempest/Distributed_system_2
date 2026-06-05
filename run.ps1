# run.ps1 — starts the full Disaster Early-Warning System on Windows
# Usage: .\run.ps1
# Requires: Docker Desktop running, Java 21, Maven

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "    $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "    $msg" -ForegroundColor Yellow }

# ── 1. Redpanda ──────────────────────────────────────────────────────────────
Write-Step "Starting Redpanda (docker compose up -d)..."
docker compose -f "$root\compose.yaml" up -d
if ($LASTEXITCODE -ne 0) { Write-Error "docker compose up failed."; exit 1 }

Write-Step "Waiting for Redpanda to become healthy..."
$attempts = 0
$maxAttempts = 30          # 30 × 3 s = 90 s max
do {
    Start-Sleep -Seconds 3
    $attempts++
    $health = docker compose -f "$root\compose.yaml" exec -T redpanda `
                rpk cluster health 2>&1
    if ($health -match "Healthy:.*true") {
        Write-Ok "Redpanda is healthy (attempt $attempts)."
        break
    }
    Write-Warn "Still waiting... ($attempts/$maxAttempts)"
    if ($attempts -ge $maxAttempts) {
        Write-Error "Redpanda did not become healthy in time. Check: docker compose logs redpanda"
        exit 1
    }
} while ($true)

# ── 2. Local EOC ─────────────────────────────────────────────────────────────
Write-Step "Opening Local EOC (port 8081) in a new window..."
Start-Process powershell -ArgumentList "-NoExit", "-Command",
    "mvn -f '$root\local_eoc\pom.xml' -q -DskipTests spring-boot:run" `
    -WorkingDirectory "$root\local_eoc"

Write-Warn "Waiting 20 s for Local EOC to finish starting..."
Start-Sleep -Seconds 20

# ── 3. National EOC ──────────────────────────────────────────────────────────
Write-Step "Opening National EOC (port 8090) in a new window..."
Start-Process powershell -ArgumentList "-NoExit", "-Command",
    "mvn -f '$root\national_eoc\pom.xml' -q -DskipTests spring-boot:run" `
    -WorkingDirectory "$root\national_eoc"

Write-Warn "Waiting 15 s for National EOC to finish starting..."
Start-Sleep -Seconds 15

# ── 4. Sensor Simulator ───────────────────────────────────────────────────────
Write-Step "Opening Sensor Simulator in a new window..."
Start-Process powershell -ArgumentList "-NoExit", "-Command",
    "mvn -f '$root\sensor_simulating_service\pom.xml' -q -DskipTests spring-boot:run" `
    -WorkingDirectory "$root\sensor_simulating_service"

# ── Summary ───────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║            ALL SERVICES STARTED                             ║" -ForegroundColor Green
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "║  Local EOC        →  http://localhost:8081                  ║" -ForegroundColor Green
Write-Host "║  National EOC     →  http://localhost:8090                  ║" -ForegroundColor Green
Write-Host "║  Redpanda Console →  http://localhost:8080                  ║" -ForegroundColor Green
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "║  Test:  see README.md § How to Test                         ║" -ForegroundColor Green
Write-Host "║  Stop:  close each window + run  docker compose down        ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
