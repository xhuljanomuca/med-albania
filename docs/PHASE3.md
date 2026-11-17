# MedLearn Phase 3 — Quizzes and examinations

## Scope
New `exam-service` with its own PostgreSQL `medlearn_exams` database and Flyway V1 migration. Questions contain exactly four bilingual alternatives, one server-only correct index, optional bilingual explanations, a configured passing threshold (default 70%), time limit, maximum attempts, and randomized question order. A learner receives questions without the correct answer. Submissions are graded on the server and stored as immutable attempts. A passed chapter quiz calls an internal, secret-authenticated course endpoint to mark chapter completion; the previous public manual completion endpoint has been removed. Final quiz access checks that every chapter has been completed.

## Endpoints
- `POST /api/exams/admin/quizzes` — create quiz draft, chapterId null for final
- `POST /api/exams/admin/quizzes/{id}/questions` — create bilingual four-choice question in draft
- `POST /api/exams/admin/quizzes/{id}/publish` — publish with questions
- `GET /api/exams/admin/quizzes/{id}/questions` — admin-only correct answer access
- `GET /api/exams/admin/analytics` — aggregate attempt analytics
- `GET /api/exams/course/{courseId}` — list currently accessible quizzes
- `POST /api/exams/{id}/start` — create attempt, return question order and expiry
- `POST /api/exams/attempts/{id}/submit` — grade once and unlock chapter on pass
- `GET /api/exams/me/attempts` — learner's own attempt history
- `GET /api/courses/{id}/learn` — chapter completion percentage, locked chapter metadata

## Caveats / outstanding production work
- NOT build-tested with Java 25, Maven or Docker in this environment.
- Admin UI only creates quiz drafts and views analytics; question bank editor is API-only.
- Attempts use an expiry timestamp but no background auto-submit; expired submissions are rejected.
- A timed-out attempt counts toward max attempts. No retry recovery for an interrupted attempt.
- Chapter unlock internal HTTP call and attempt DB commit are not atomic; implement an outbox/reconciliation worker.
- The internal shared secret is a development stopgap; production should use mTLS and service identities, and restrict internal endpoints at the network layer.
- Course-service internal completion endpoint does not independently verify quiz success; the exam-service alone must possess the secret.
- JWT issuer uses localhost while JWKs resolve internally; verify with actual Keycloak-issued tokens in the target environment.
- Existing gateway uses a simplistic proxy and may mishandle streaming/multipart; replace with supported Spring Cloud Gateway routes before production.
- Existing user profile entity exposes sensitive fields in responses; replace with explicit response DTOs and data minimization.
- Existing payment PDF validation checks only signature and size, not malware scanning.
- Bilingual course content is supported; backend error messages and email localization are not complete.
- The application has no proven end-to-end Keycloak, PostgreSQL, or browser test yet.

## Suggested validation commands
```bash
cp .env.example .env # replace all secrets
for d in services/*; do (cd "$d" && mvn -B test package) || exit 1; done
(cd web && npm ci && npm run build)
docker compose config
docker compose up --build -d
docker compose ps
```

For the initial local run with an existing Phase 2 PostgreSQL volume, create `medlearn_exams` manually (the init SQL only executes for a fresh volume), or use a database migration/creation procedure. Never delete a persistent volume containing user data merely to recreate the database.
