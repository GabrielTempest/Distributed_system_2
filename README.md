# Disaster Alert System - Setup Guide

## **Step 1: Start Kafka & Zookeeper (Docker)**

```bash
# Navigate to project root, example:
cd e:/VGU_Summer_26-26/Distributed_System/Distributed_system_2 

# Start Kafka and Zookeeper containers
docker compose up -d

# Verify they're running
docker compose ps
```

**Expected output:**

```t
NAME                COMMAND                  SERVICE      STATUS
kafka               "/etc/confluent/dock…"   kafka        Up 2 seconds
zookeeper           "/etc/confluent/dock…"   zookeeper    Up 3 seconds
```

**Check logs if needed:**

```bash
docker compose logs kafka    # See Kafka startup messages
docker compose logs zookeeper
```

---

## **Step 2: Start Local EOC (Port 8080)**

Open **Terminal 1** and run:

```bash
cd e:/VGU_Summer_26-26/Distributed_System/Distributed_system_2/local_eoc

# Option A: Maven (compiles + runs)
mvn spring-boot:run

# Option B: If already compiled
java -jar target/local_eoc-0.0.1-SNAPSHOT.jar
```

**Expected output:**

```t
2026-05-31 10:04:00 [main] INFO o.s.b.w.e.t.TomcatWebServer - Tomcat started on port(s): 8080
2026-05-31 10:04:00 [main] INFO d.l.LocalEocApplication - Started LocalEocApplication
```

**Verify it's running:**

```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

---

## **Step 3: Start National EOC (Port 8081)**

Open **Terminal 2** and run:

```bash
cd e:/VGU_Summer_26-26/Distributed_System/Distributed_system_2/national_eoc

# Option A: Maven
mvn spring-boot:run

# Option B: If already compiled
java -jar target/national_eoc-0.0.1-SNAPSHOT.jar
```

**Expected output:**

```t
2026-05-31 10:04:05 [main] INFO o.s.b.w.e.t.TomcatWebServer - Tomcat started on port(s): 8081
2026-05-31 10:04:05 [main] INFO d.n.NationalEocApplication - Started NationalEocApplication
```

---

## **Step 4: Start Sensor Simulator (Port 8000 - console app)**

Open **Terminal 3** and run:

```bash
cd e:/VGU_Summer_26-26/Distributed_System/Distributed_system_2/sensor_simulating_service

# Option A: Maven
mvn spring-boot:run

# Option B: If already compiled
java -jar target/sensor_simulating_service-0.0.1-SNAPSHOT.jar
```

**Expected output:**

```t
2026-05-31 10:04:10 [main] INFO o.s.b.SpringApplication - Started SensorSimulatingService
[sensor-simulator] Starting to generate sensor readings...
```

---

## System State After Starting All Services

```t
Terminal 1: Local EOC
├─ Listening on: http://localhost:8080
├─ Status: RUNNING
└─ Mode: HTTP Server + Kafka Producer

Terminal 2: National EOC
├─ Status: RUNNING
└─ Mode: Kafka Consumer Worker

Terminal 3: Sensor Simulator
├─ Status: RUNNING
└─ Mode: Generates readings → POSTs to Local EOC

Docker Containers:
├─ Zookeeper: Running on :2181
├─ Kafka: Running on :9092
└─ Topic: "disaster-alerts" (will be created automatically)
```

---

## Testing End-to-End

### **Test 1: Verify Kafka is Connected**

```bash
# From any terminal, check if Kafka has the topic
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
docker ps

# Expected output includes eventual:
# disaster-alerts
```

### **Test 2: Send Manual Sensor Reading to Local EOC**

```bash
curl -X POST http://localhost:8080/api/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "sensorId": "SENSOR-001",
    "sensorType": "RAINGAUGE",
    "value": 450.5,
    "unit": "mm",
    "timestamp": "2026-05-31T10:04:00Z"
  }'
```

**Expected response:**

```json
{
  "sensorId": "SENSOR-001",
  "sensorType": "RAINGAUGE",
  "value": 450.5,
  "unit": "mm",
  "timestamp": "2026-05-31T10:04:00Z"
}
```

### **Test 3: View Kafka Messages in Real-Time**

```bash
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic disaster-alerts \
  --from-beginning \
  --timeout-ms 3000
```

**Expected output (after Local EOC processes):**

```json
{
  "event_id": "a1b2c3d4-e5f6-47a8-9b0c-1d2e3f4a5b6c",
  "area_id": "HCM",
  "timestamp": "2026-05-31T10:04:15Z",
  "disasterType": "FLOOD",
  "alertLevel": "RED",
  "measurements": [...]
}
```

---

## Stopping Everything

**When you're done:**

```bash
# Stop Kafka and Zookeeper
docker compose down

# Stop all services (Ctrl+C in each terminal)
# Terminal 1: Ctrl+C
# Terminal 2: Ctrl+C
# Terminal 3: Ctrl+C
```

---

## 📋 Quick Reference: Terminal Layout

```t
┌─────────────────────────────────────────────────────────────┐
│ Terminal 1: Local EOC (8080)                                │
│ $ cd local_eoc && mvn spring-boot:run                       │
├─────────────────────────────────────────────────────────────┤
│ Terminal 2: National EOC (8081)                             │
│ $ cd national_eoc && mvn spring-boot:run                    │
├─────────────────────────────────────────────────────────────┤
│ Terminal 3: Sensor Simulator                                │
│ $ cd sensor_simulating_service && mvn spring-boot:run       │
├─────────────────────────────────────────────────────────────┤
│ Terminal 4: Docker Compose + Testing                        │
│ $ docker compose up -d                                      │
│ $ curl http://localhost:8080/api/sensors ...                │
└─────────────────────────────────────────────────────────────┘
```

---

## Debugging

**If Local EOC won't start:**

```bash
# Check if port 8080 is already in use
netstat -ano | findstr :8080

# Kill the process using port 8080
taskkill /PID <PID> /F
```

**If Kafka connection fails:**

```bash
# Check Docker containers
docker ps

# View Docker logs
docker compose logs

# Restart everything
docker compose restart
```

**If you see "Connection refused":**

```bash
# Make sure Docker Desktop is running (Windows)
# Make sure containers are up
docker compose up -d
```

---

## Full Data Flow Once Running

```t
[Sensor Simulator]
      ↓ (generates readings every 5s)
POST http://localhost:8080/api/sensors
      ↓
[Local EOC HTTP Endpoint receives]
      ↓ (stores in memory cache)
[Async batch processor triggers]
      ↓ (processes every 10 seconds)
[Analyzes DisasterType]
      ↓
[KafkaTemplate.send() publishes]
      ↓
[Kafka Topic: disaster-alerts]
      ↓
[National EOC consumes via @KafkaListener]
      ↓
[Dashboard updates / Alert sent]
```

---

## Ready?

Once all 4 terminals are running, your system is **fully operational**. The data should flow:

- Sensor Simulator → Local EOC (HTTP POST)
- Local EOC → Kafka (producer)
- Kafka → National EOC (consumer)
