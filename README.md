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
| **National EOC** | *(no HTTP server)* | Consumes alerts from all local EOCs |
| **Sensor Simulator** | *(no HTTP server)* | Generates fake readings and POSTs them to Local EOC |
| Database Writer |	*(no HTTP server)* | Consumes alerts from Redpanda and writes them to PostgreSQL |

**Kafka topic:** `disaster-alerts` (3 partitions, 1 replica)  
**Alert key:** `disasterType` name — so messages for the same disaster type always go to the same partition.

---

## Prerequisites

- **Docker Desktop** running
- **Java 21** (`java -version`)
- **Maven 3.9+** (`mvn -version`)

---

## Start Guide (step by step)

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

### Step 2 — Start Services
- Currently available 3 services: `national_eoc`, `local_eoc` and `database_writer`
- 3 services can be start in any other, however, please note that `consumers` lies in `local_eoc` nodes.
- You can start as many `local_eoc` and `national_eoc` as wish, but you would need to change server port in `application.properties` of the nodes if you run muultiple `local_eoc`/`national_eoc` on the same device.

#### Start Local EOC Node
```powershell
cd local_eoc
mvn spring-boot:run
```

### Start National EOC Node
```powershell
cd national_eoc
mvn spring-boot:run
```

### Start Database Writer Service
```powershell
cd database_writer
mvn spring-boot:run
```

### Step 3 — Start Sensor Simulator
```powershell
cd sensor_simulating_service
mvn -q -DskipTests spring-boot:run
```
- `sensor_simulating_service` should run on the same device as `local_eoc` so that `local_eoc` can consume readings to generate alerts.
- Many `sensor_simulating_service` can run on the same devices, however, they will only POST http requests to the `local_eoc` with the default port *8081*.
- If you want the `sensor_simulating_service` to POST requests to another additional `local_eoc` node on the device / on another device, you should change *hostname* and *port* in `api.base-url` in `application.properties` of the `sensor_simulating_service`
- The simulator has no web server — it just starts posting readings to `http://localhost:8081/api/readings` immediately.

---

## How to Test

### Watch the live data flow

The `Local EOC` node flushes its sensor buffer every **5 seconds**, then process the batch to generate alerts and publish them to the Kafka topic `disaster-alerts`

**Local EOC terminal** — look for log in the console to see if it works well.

**National EOC terminal** — both the monitor dashboard and log console can be used to test feature.

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

## Stopping Everything

```powershell
# Stop Spring Boot services: 
#      Ctrl+C in each terminal

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

**Port 8080 already in use (Redpanda Console won't start)**
- Another process is on 8080. Find it: `netstat -ano | findstr :8080` then `taskkill /PID <PID> /F`.

**Redpanda stays `unhealthy`**
- Give it another 30 s. If it stays unhealthy: `docker compose logs redpanda`.
- Restart: `docker compose down && docker compose up -d`.

**First run is slow**
- Maven downloads ~200 MB of dependencies on the first build. Subsequent runs use the local cache and start in seconds.
