# Running Task 1: Duplicate Nomination Demo

This is a working implementation of the solution in `workflow.md`: a React
frontend, a Spring Boot backend, and PostgreSQL, wired so that nominating the
same officer twice for the same training programme is rejected instead of
silently accepted.

## 1. Database (PostgreSQL)

Create the database once:

```sql
CREATE DATABASE training_db;
```

The backend expects `localhost:5432`, user `postgres`, password `postgres`
(edit `backend/src/main/resources/application.properties` if yours differ).
Tables are created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`).

## 2. Backend (Spring Boot, port 8080)

```bash
cd backend
./gradlew bootRun
```

On first run it seeds 5 departments, 8 officers and 1 sample training
programme so there's real data to nominate against.

Key endpoints:

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/departments` | list departments |
| GET | `/api/officers` | list officers (optional `?departmentId=`) |
| GET | `/api/programmes` | list training programmes |
| POST | `/api/programmes` | create a training programme |
| GET | `/api/nominations?programmeId=` | list nominations for a programme |
| POST | `/api/nominations` | submit a nomination — **returns `409 Conflict`** if the officer is already nominated for that programme |

## 3. Frontend (React + Vite, port 5173)

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173.

## Try the duplicate check

1. Pick the seeded programme, pick a department (e.g. *Finance Division*)
   and an officer (e.g. *A. Perera*), submit — it's accepted as **CONFIRMED**.
2. Pick a **different department** (e.g. *Administration Division*) but the
   **same officer**, submit again — the backend rejects it with `409` and the
   UI shows: *"Officer 'A. Perera' was already nominated for this programme
   by Finance Division on …"*, exactly the scenario in the Task 1 example.
