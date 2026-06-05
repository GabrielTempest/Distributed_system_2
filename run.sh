#!/usr/bin/env bash
# run.sh — starts the full Disaster Early-Warning System on Linux / macOS / WSL
# Usage: chmod +x run.sh && ./run.sh
# Requires: Docker (compose v2), Java 21, Maven

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT/.logs"
mkdir -p "$LOG_DIR"

step()  { echo -e "\n\033[36m==> $*\033[0m"; }
ok()    { echo -e "    \033[32m$*\033[0m"; }
warn()  { echo -e "    \033[33m$*\033[0m"; }

cleanup() {
    echo ""
    warn "Shutting down Spring Boot services..."
    [ -n "${LOCAL_PID:-}"  ] && kill "$LOCAL_PID"  2>/dev/null || true
    [ -n "${NATIONAL_PID:-}" ] && kill "$NATIONAL_PID" 2>/dev/null || true
    [ -n "${SIM_PID:-}"    ] && kill "$SIM_PID"    2>/dev/null || true
    step "Stopping Redpanda..."
    docker compose -f "$ROOT/compose.yaml" down
    ok "All stopped."
}
trap cleanup EXIT INT TERM

# ── 1. Redpanda ───────────────────────────────────────────────────────────────
step "Starting Redpanda (docker compose up -d)..."
docker compose -f "$ROOT/compose.yaml" up -d

step "Waiting for Redpanda to become healthy..."
attempts=0
max_attempts=30   # 30 × 3 s = 90 s max
until docker compose -f "$ROOT/compose.yaml" exec -T redpanda \
        rpk cluster health 2>/dev/null | grep -q "Healthy:.*true"; do
    attempts=$((attempts + 1))
    if [ "$attempts" -ge "$max_attempts" ]; then
        echo "ERROR: Redpanda did not become healthy in time."
        echo "       Check: docker compose logs redpanda"
        exit 1
    fi
    warn "Still waiting... ($attempts/$max_attempts)"
    sleep 3
done
ok "Redpanda is healthy."

# ── 2. Local EOC ─────────────────────────────────────────────────────────────
step "Starting Local EOC (port 8081)..."
mvn -f "$ROOT/local_eoc/pom.xml" -q -DskipTests spring-boot:run \
    > "$LOG_DIR/local_eoc.log" 2>&1 &
LOCAL_PID=$!
ok "PID $LOCAL_PID  →  logs: $LOG_DIR/local_eoc.log"

warn "Waiting 20 s for Local EOC to start..."
sleep 20

# ── 3. National EOC ───────────────────────────────────────────────────────────
step "Starting National EOC (port 8090)..."
mvn -f "$ROOT/national_eoc/pom.xml" -q -DskipTests spring-boot:run \
    > "$LOG_DIR/national_eoc.log" 2>&1 &
NATIONAL_PID=$!
ok "PID $NATIONAL_PID  →  logs: $LOG_DIR/national_eoc.log"

warn "Waiting 15 s for National EOC to start..."
sleep 15

# ── 4. Sensor Simulator ───────────────────────────────────────────────────────
step "Starting Sensor Simulator..."
mvn -f "$ROOT/sensor_simulating_service/pom.xml" -q -DskipTests spring-boot:run \
    > "$LOG_DIR/sensor_sim.log" 2>&1 &
SIM_PID=$!
ok "PID $SIM_PID  →  logs: $LOG_DIR/sensor_sim.log"

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
echo -e "\033[32m╔══════════════════════════════════════════════════════════════╗\033[0m"
echo -e "\033[32m║            ALL SERVICES STARTED                             ║\033[0m"
echo -e "\033[32m╠══════════════════════════════════════════════════════════════╣\033[0m"
echo -e "\033[32m║  Local EOC        →  http://localhost:8081                  ║\033[0m"
echo -e "\033[32m║  National EOC     →  http://localhost:8090                  ║\033[0m"
echo -e "\033[32m║  Redpanda Console →  http://localhost:8080                  ║\033[0m"
echo -e "\033[32m╠══════════════════════════════════════════════════════════════╣\033[0m"
echo -e "\033[32m║  Tail logs:  tail -f .logs/national_eoc.log                 ║\033[0m"
echo -e "\033[32m║  Stop:       Ctrl+C  (cleans up everything automatically)   ║\033[0m"
echo -e "\033[32m╚══════════════════════════════════════════════════════════════╝\033[0m"
echo ""

# Keep the script alive so Ctrl+C triggers cleanup
step "Tailing National EOC log (Ctrl+C to stop all)..."
tail -f "$LOG_DIR/national_eoc.log"
