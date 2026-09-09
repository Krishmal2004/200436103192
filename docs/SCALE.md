# Scaling This Solution

This document explains how the current Task 1 solution (Spring Boot + PostgreSQL backend,
React/Vite frontend) would need to evolve to support a large number of concurrent users,
instead of the single-team demo it is today. It's organized as "what breaks first" and
"what to do about it," referencing the actual code so it's concrete rather than generic.

## 1. What the current solution actually is

- **Backend**: one Spring Boot instance, one PostgreSQL database, no caching, no auth.
- **Frontend**: a Vite/React SPA that calls the backend directly via `axios`
  (`frontend/src/api.js`), hardcoded to `http://localhost:8080/api`.
- **Data model**: `Officer`, `Department`, `TrainingProgramme`, `Nomination`, with a
  `UNIQUE(officer_id, programme_id)` constraint on `nominations` as the source of truth
  for "no duplicate nominations" (`backend/.../entity/Nomination.java`).
- **Schema management**: `spring.jpa.hibernate.ddl-auto=update` — Hibernate mutates the
  schema on boot (`application.properties`). Fine for a demo, unsafe for production.
- **CORS**: hardcoded to `http://localhost:5173` in `WebConfig.java`.

This is a perfectly reasonable shape for the assignment. Everything below describes what
changes as the number of users grows, roughly in the order it would actually bite.

## 2. What breaks first (small → medium: hundreds–thousands of users)

| Problem | Where | Fix |
|---|---|---|
| Every "list" endpoint returns the whole table | `GET /api/officers`, `/departments`, `/programmes` | Add pagination (`Pageable`) and, for officers, a search/filter query param instead of shipping every officer to the browser on every page load. |
| `ddl-auto=update` | `application.properties` | Switch to versioned migrations (Flyway or Liquibase). `update` can silently pick the wrong column type or fail to drop things as the schema evolves, and it's not repeatable across environments. |
| Hardcoded CORS origin / API base URL | `WebConfig.java`, `frontend/src/api.js` | Externalize both to environment/config (`application-{profile}.properties`, `import.meta.env.VITE_API_BASE_URL`) so the same build can point at staging/prod. |
| No authentication/authorization | entire backend | Add real auth (JWT/OAuth2 via Spring Security) once this isn't a single trusted team - right now anyone who can reach the API can nominate on any department's behalf. |
| N+1-ish confirmed-count query | `ProgrammeController.toResponse` runs one `COUNT` query per programme, per request | Fine at small scale; once there are many programmes listed at once, either add a DB index on `(programme_id, status)` or maintain a denormalized `confirmedCount` column updated transactionally on nomination insert/cancel. |
| No connection pool tuning | default HikariCP settings | Set explicit `spring.datasource.hikari.maximum-pool-size` sized to the DB's actual connection limit, not the default guess. |

At this stage the app is still a single instance + single database - you're just removing
landmines, not changing the architecture.

## 3. Medium → large (tens of thousands of users, real concurrency)

**Backend is already stateless**, which is the important precondition for horizontal
scaling: there's no server-side session, no in-memory state kept between requests. That
means the next step is mechanical rather than a rewrite:

1. **Run multiple backend instances behind a load balancer** (e.g. an ALB/NGINX, or a
   Kubernetes Deployment with a Service). Any instance can serve any request.
2. **Move the database off the same box**, onto a managed PostgreSQL service (RDS/Cloud
   SQL/Azure Database) with:
   - **Read replicas** for the read-heavy endpoints (`GET /api/officers`,
     `/departments`, `/programmes`, nomination listings) — these vastly outnumber writes
     (nominating).
   - Automated backups and point-in-time recovery.
3. **Add a cache** (Redis) in front of rarely-changing reference data — departments and
   the officer directory don't change per-request, so cache them with a short TTL or
   invalidate on write, instead of hitting Postgres on every page load.
4. **Serve the frontend as static assets from a CDN** (`npm run build` output), not from
   the dev server. It's a pure SPA with no server rendering, so this is a direct win with
   no code changes.
5. **Rate limit the write endpoints** (`POST /api/nominations`, `POST /api/programmes`),
   e.g. per department or per API key, so one misbehaving client can't starve the
   nomination waitlist logic for everyone else.

## 4. The concurrency question this app actually has to get right

The interesting correctness problem here isn't "many reads," it's **many officers being
nominated for the same limited-capacity programme at the same instant** - a classic
race condition, and the codebase already has the right instinct for it:

- `NominationService.createNomination` does an **application-level duplicate check**
  first (fast, gives a friendly error), then **falls back to the database's unique
  constraint** (`uk_officer_programme`) to catch the case where two requests raced past
  the application check at the same moment (`backend/.../service/NominationService.java`).

That pattern (optimistic app-level check + DB constraint as the real guarantee) is the
correct one and it **scales horizontally as-is**, because the guarantee lives in the
database, not in any one instance's memory. The one thing worth adding as volume grows:

- The **confirmed-vs-waitlisted decision** (`confirmedCount < maxParticipants`) is
  currently a read-then-write with only the transaction boundary protecting it. Under
  heavy concurrent load on a single very popular programme, this is the one place that
  could theoretically let more than `maxParticipants` slip through in the same race-y way
  duplicates almost did. Fix by making the capacity check itself atomic - either a
  `SELECT ... FOR UPDATE` on the programme row inside the transaction, or a DB-level
  check constraint/trigger - rather than relying purely on the app-level count query.

## 5. Very large scale (hundreds of thousands+, multi-region)

This is well beyond what the assignment needs, but for completeness:

- **Partition/shard by tenant** (e.g. by ministry/department cluster) if a single
  Postgres instance's write throughput becomes the ceiling - nominations are naturally
  partitionable by department or programme.
- **Move slow/non-critical work off the request path** via a message queue (e.g. sending
  confirmation emails, generating attendance certificates) instead of doing it inline in
  `createNomination`.
- **Multi-region read replicas + a regional API gateway** if users are geographically
  spread and latency to a single region matters.
- **Full observability**: structured logging, request tracing (OpenTelemetry), and
  dashboards on the two numbers that actually matter for this domain - nomination
  request latency and confirmed/waitlisted conversion rate per programme - so capacity
  problems show up before users complain.

## 6. Summary: the actual order of operations

1. Pagination + search on list endpoints, Flyway migrations, externalized config/CORS,
   basic auth. *(Needed almost immediately outside a demo.)*
2. Multiple stateless backend instances + load balancer, managed Postgres with read
   replicas, Redis cache for reference data, CDN for the static frontend.
3. Make the capacity check (`confirmedCount < maxParticipants`) row-locked/atomic, since
   that's the one race condition the current unique-constraint trick doesn't fully cover.
4. Only if truly necessary: sharding, message queues, multi-region.

The good news: nothing here requires re-architecting the domain model. The
department/officer/programme/nomination shape and the unique-constraint-as-safety-net
pattern both scale - the work is almost entirely in operations (migrations, pooling,
replicas, caching) plus one concurrency tightening on the capacity check.
