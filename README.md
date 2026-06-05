# Disaster Early-Warning System

A distributed system that ingests sensor readings, classifies disaster risk levels, and propagates alerts through a Redpanda (Kafka-compatible) message bus.

```
[Sensor Simulator] --HTTP POST--> [Local EOC] --Kafka--> [National EOC]
     :8081 (target)                   :8081                   :8090
```

---

## Architecture

| Service | Port | Role |
|---|---|---|
| **Redpanda broker** | `19092` (host) / `9092` (container) | Kafka-compatible message bus |
| **Redpanda Console** | `8080` | Web UI — browse topics and messages |
| **Pandaproxy** | `8082` | REST proxy for Redpanda |
| **Local EOC** | `8081` | Receives sensor readings, classifies risk, publishes alerts |
| **National EOC** | `8090` | Consumes alerts from all local EOCs |
| **Sensor Simulator** | *(no HTTP server)* | Generates fake readings and POSTs them to Local EOC |

**Kafka topic:** `disaster-alerts` (3 partitions, 1 replica)
**Alert key:** `disasterType` name — so messages for the same disaster type always go to the same partition.

---

## Prerequisites

- **Docker Desktop** running
- **Java 21** (`java -version`)
- **Maven 3.9+** (`mvn -version`) — or use the `mvnw` wrapper inside each module

---

## Quick Start (automated)

### Windows (PowerShell)

```powershell
.\run.ps1
```

Opens Redpanda, then spawns a new terminal window for each Spring Boot service in the correct startup order.

### Linux / macOS / WSL

```bash
chmod +x run.sh && ./run.sh
```

Starts all services as background processes and tails their logs in the foreground.

---

## Manual Start (step by step)

### Step 1 — Start Redpanda

```bash
docker compose up -d
```

Wait until healthy (≈ 15 s):

```bash
docker compose ps
# redpanda   Up (healthy)
# redpanda-console   Up
```

The `redpanda-init` container also runs once to pre-create the `disaster-alerts` topic and then exits — this is normal.

### Step 2 — Start Local EOC (Terminal 1)

```powershell
cd local_eoc
mvn -q -DskipTests spring-boot:run
```

Expected log line: `Tomcat started on port(s): 8081`

### Step 3 — Start National EOC (Terminal 2)

```powershell
cd national_eoc
mvn -q -DskipTests spring-boot:run
```

Expected log line: `Tomcat started on port(s): 8090`

### Step 4 — Start Sensor Simulator (Terminal 3)

```powershell
cd sensor_simulating_service
mvn -q -DskipTests spring-boot:run
```

The simulator has no web server — it just starts posting readings to `http://localhost:8081/api/readings` immediately.

---

## How to Test

### Watch the live data flow

After all four services are running, the Local EOC flushes its sensor buffer every **5 seconds**. If the computed risk level for any disaster type changes, it publishes an alert.

**Local EOC terminal** — look for:
```
Published alert <uuid> [FLOOD:YELLOW] → partition 1
```

**National EOC terminal** — look for:
```
>>> ALERT RECEIVED from LOCAL-EOC-01: [FLOOD / YELLOW] at 2026-... — FLOOD alert YELLOW. Readings: FLOODGAUGE=10.23 RAINGAUGE=247.88
```

### Send a manual high-severity reading

Trigger an immediate ORANGE/RED flood alert by injecting extreme sensor values:

```bash
# Windows PowerShell
Invoke-WebRequest -Uri http://localhost:8081/api/readings `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"sensorId":"TEST-001","sensorType":"FLOODGAUGE","value":19.5,"unit":"m","timestamp":"2026-01-01T00:00:00Z"}'

Invoke-WebRequest -Uri http://localhost:8081/api/readings `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"sensorId":"TEST-002","sensorType":"RAINGAUGE","value":480.0,"unit":"mm","timestamp":"2026-01-01T00:00:00Z"}'
```

```bash
# curl (WSL / macOS / Linux)
curl -s -X POST http://localhost:8081/api/readings \
  -H "Content-Type: application/json" \
  -d '{"sensorId":"TEST-001","sensorType":"FLOODGAUGE","value":19.5,"unit":"m","timestamp":"2026-01-01T00:00:00Z"}'

curl -s -X POST http://localhost:8081/api/readings \
  -H "Content-Type: application/json" \
  -d '{"sensorId":"TEST-002","sensorType":"RAINGAUGE","value":480.0,"unit":"mm","timestamp":"2026-01-01T00:00:00Z"}'
```

Wait up to 5 seconds for the flush cycle. You should see a FLOOD RED or ORANGE alert appear in the National EOC log.

### Inspect messages via Redpanda Console

Open **http://localhost:8080** in a browser.  
Navigate to **Topics → disaster-alerts → Messages** to see all published alerts as JSON:

```json
{
  "alertId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "localEocId": "LOCAL-EOC-01",
  "disasterType": "FLOOD",
  "alertType": "RED",
  "message": "FLOOD alert RED. Readings: FLOODGAUGE=19.50 RAINGAUGE=480.00",
  "timestamp": "2026-05-31T10:04:15.123456Z"
}
```

### Inspect messages via rpk CLI

```bash
docker compose exec redpanda rpk topic consume disaster-alerts \
  --brokers localhost:9092 \
  --offset start
```

### List topics

```bash
docker compose exec redpanda rpk topic list --brokers localhost:9092
```

---

## Alert Classification

The Local EOC averages all sensor readings received in the last 5-second window, normalises each relevant sensor value to a 0–1 severity score, and maps the worst score to an alert level:

| Severity | Alert level |
|---|---|
| < 0.40 | GREEN (no alert published) |
| 0.40 – 0.59 | YELLOW |
| 0.60 – 0.79 | ORANGE |
| ≥ 0.80 | RED |

Alerts are **de-duplicated**: a new message is only published when the level *changes* (including a downgrade back to GREEN).

**Sensor → normalisation direction:**

| Sensor | Range | Danger direction |
|---|---|---|
| FLOODGAUGE | 0 – 20 m | High = danger |
| RAINGAUGE | 0 – 500 mm | High = danger |
| ANEMOMETER | 0 – 60 m/s | High = danger |
| BAROMETER | 300 – 1100 hPa | **Low** = danger (inverted) |
| SOILMOISTURE | 0 – 100 % | High = danger |
| TILT | −90 – 90 deg | Absolute value = danger |
| VIBRATION | 0 – 16 g | High = danger |

---

## Stopping Everything

```powershell
# Stop Spring Boot services: Ctrl+C in each terminal (or close the windows opened by run.ps1)

# Stop Redpanda
docker compose down
```

To also remove stored Redpanda data:
```bash
docker compose down -v
```

---

## Troubleshooting

**`Connection refused` when Local EOC starts**
- Docker Desktop must be running before `docker compose up -d`.
- Check: `docker compose ps` — all services should show `Up`.

**National EOC logs `ClassNotFoundException` or `__TypeId__ header` errors**
- Ensure `spring.kafka.consumer.properties.spring.json.use.type.headers=false` is present in `national_eoc/src/main/resources/application.properties`.

**Sensor Simulator fails to POST**
- Confirm Local EOC is running on port 8081 (`netstat -ano | findstr :8081`).
- `api.base-url` in `sensor_simulating_service/src/main/resources/application.properties` must be `http://localhost:8081`.

**Port 8080 already in use (Redpanda Console won't start)**
- Another process is on 8080. Find it: `netstat -ano | findstr :8080` then `taskkill /PID <PID> /F`.

**Redpanda stays `unhealthy`**
- Give it another 30 s. If it stays unhealthy: `docker compose logs redpanda`.
- Restart: `docker compose down && docker compose up -d`.

**First run is slow**
- Maven downloads ~200 MB of dependencies on the first build. Subsequent runs use the local cache and start in seconds.
