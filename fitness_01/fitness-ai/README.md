# FitAI - AI-Powered Fitness Application

Track workouts, view progress, and get AI-generated general fitness suggestions (Google Gemini).

## Architecture

```
React (5173) -> API Gateway (8080, validates Keycloak JWT)
                 |-- user-service     (8081)  profiles          -> MySQL fitness_user
                 |-- activity-service (8082)  workouts          -> MySQL fitness_activity
                 |        '-- publishes WorkoutCompleted --> RabbitMQ (5672)
                 '-- ai-service       (8083)  consumes event -> Gemini -> MySQL fitness_ai
Eureka (8761) discovery | Config Server (8888) config | Keycloak (8180) identity
```

Flow: sign in -> log workout -> activity saved -> event published after DB commit -> AI service calls Gemini -> recommendation stored -> React polls `GET /api/recommendations/{activityId}` (404 until ready).

## Prerequisites
Java 21, Maven 3.9+, Node.js LTS, Docker Desktop, a Gemini API key (optional; without it a safe fallback tip is stored).

## Run (local)

```bash
# 1. Infrastructure: MySQL, RabbitMQ, Keycloak (realm "fitness" auto-imported)
docker compose up -d

# 2. Build all services
mvn clean package -DskipTests

# 3. Start in this order, each in its own terminal
java -jar eureka-server/target/eureka-server-1.0.0.jar
java -jar config-server/target/config-server-1.0.0.jar
java -jar user-service/target/user-service-1.0.0.jar
java -jar activity-service/target/activity-service-1.0.0.jar
GEMINI_API_KEY=xxxx java -jar ai-service/target/ai-service-1.0.0.jar
java -jar api-gateway/target/api-gateway-1.0.0.jar

# 4. Frontend
cd frontend && npm install && npm run dev      # http://localhost:5173
```

Test users (Keycloak): `demo / demo123` (USER), `admin / admin123` (USER + ADMIN). Self-registration is enabled.
Keycloak admin console: http://localhost:8180 (admin / admin). RabbitMQ UI: http://localhost:15672 (guest / guest).

## REST API (via gateway, Bearer token required)

| Method | Path | Purpose |
|---|---|---|
| POST | /api/users/profile | Create/update fitness profile |
| GET | /api/users/me | Current profile (404 if none) |
| POST | /api/activities | Save workout |
| GET | /api/activities | Workout history |
| GET | /api/recommendations/{activityId} | AI suggestion (404 until generated) |
| GET | /api/admin/users | All profiles (ADMIN role only) |

Example body for `POST /api/activities`:
```json
{ "type": "RUNNING", "durationMinutes": 30, "distanceKm": 4.2, "caloriesBurned": 280 }
```
Get a token for Postman:
```bash
curl -s -d "client_id=fitness-frontend&grant_type=password&username=demo&password=demo123" \
  http://localhost:8180/realms/fitness/protocol/openid-connect/token
```

## Pre-demo checklist
- Build succeeds; all services visible at http://localhost:8761
- Profile and workout APIs work; rows appear in MySQL
- Event reaches ai-service (RabbitMQ queue `workout.completed.ai`); recommendation appears in the UI
- Invalid input -> 400; no/expired token -> 401; `demo` calling `/api/admin/users` -> 403
- Stop RabbitMQ or unset the Gemini key: workouts still save; fallback tip is used

## Security & responsible-AI notes
- Gateway validates JWTs and overwrites `X-User-Id` from the token; downstream services trust only the gateway, so do not expose ports 8081-8083 publicly (in production add resource-server validation to each service and TLS).
- Secrets come from environment variables (`GEMINI_API_KEY`, `DB_*`); keep them out of Git. Keycloak runs in dev mode with demo credentials: change before any real deployment.
- Suggestions are general guidance, not medical advice; model output is trimmed/length-capped and a disclaimer is appended.
- This is a local demo build, not a production system.
