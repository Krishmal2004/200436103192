# testing_data

Test fixtures and a seed script that reproduce the Task 2 capacity scenario
(`docs/task02_workflow.md`) against a real, running instance of the app —
useful for demoing or manually verifying the confirm/waitlist/promote
behaviour, as distinct from `NominationCapacityScenarioTest` in the backend
test suite, which proves the same behaviour but rolls its data back and
never touches a running app.

## Files

- **`participants.json`** — 60 test officers (`employeeNo`, `name`,
  `department`), round-robined across the app's 5 seeded departments.
- **`seed-60-participants.js`** — a Node script that, against a running
  backend:
  1. Creates (or reuses) a **Cybersecurity Awareness Programme** with a
     40-seat capacity.
  2. Creates each of the 60 officers in `participants.json` (skipping any
     that already exist, so it's safe to re-run).
  3. Submits all 60 as nominations, in order, and prints each one's
     resulting status.
  4. Cancels the first confirmed nomination and confirms the oldest
     waitlisted nomination was automatically promoted.

## Running it

Requires Node 18+ (uses the built-in `fetch`) and the backend running
locally (`./gradlew bootRun` from `backend/`, default `localhost:8080`).

```bash
node testing_data/seed-60-participants.js

# seed only, skip the cancel-and-promote step
node testing_data/seed-60-participants.js --no-cancel-demo

# point at a different backend
API_BASE_URL=http://localhost:8080/api node testing_data/seed-60-participants.js
```

## Cleaning up

This writes real, persisted rows (unlike the backend test, it does not roll
back). To remove everything it created:

```sql
DELETE FROM nominations WHERE submitted_by = 'testing_data/seed-60-participants.js';
DELETE FROM officers WHERE employee_no LIKE 'CYB-%';
DELETE FROM training_programmes WHERE title = 'Cybersecurity Awareness Programme';
```
