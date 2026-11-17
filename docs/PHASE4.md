# MedLearn Phase 4 — Certificates and achievements

## Added
- Java 25 Spring Boot 4 certificate-service (port 8086) with its own PostgreSQL database and Flyway V1 migration.
- JWT-protected personal certificate gallery, issue endpoint, PDF download, and admin revocation.
- Public read-only verification by unpredictable verification code; states VALID, EXPIRED, REVOKED; unknown codes return HTTP 404.
- Unique issuance per final exam attempt, checked against the exam service using a server-side internal shared secret.
- A course-completion achievement is recorded on issuance.
- OpenPDF PDF export with ZXing QR verification URL.
- English/Albanian labels and bilingual course titles in certificates; basic admin management and member gallery pages.

## API
- POST /api/certificates/issue (JWT) `{ "attemptId":"uuid", "recipientName":"...", "courseTitleSq":"...", "courseTitleEn":"..." }`
- GET /api/certificates/me (JWT)
- GET /api/certificates/me/achievements (JWT)
- GET /api/certificates/{id}/pdf (JWT, owner)
- GET /api/certificates/verify/{code} (public)
- GET /api/certificates/admin (platform_admin)
- POST /api/certificates/admin/{id}/revoke (platform_admin)
- POST /api/certificates/admin/{id}/accreditation (platform_admin)
- GET /internal/exams/attempts/{id}/certificate-eligibility (internal shared secret; not gateway routed)

## Critical limitations / work required before production
- Phase 1–3 were not demonstrated to compile or start; all integration checks remain unverified.
- Issuance is user-initiated, not automatic/event-driven. The submitted name and titles are not validated against authoritative profile/course data; do not treat PDF content as identity-verified.
- Accreditation input is administrator-controlled and lacks authoritative external evidence validation; credits must not be presented as recognized until verified by an accreditation body.
- QR uses the public verification URL and is not a digital signature. Public verification can expose recipient name; implement privacy assessment, abuse throttling, and retention policy.
- No complete certificate template editor, background jobs, event delivery, or signed immutable certificate snapshots.
- Certificate issuance idempotency has a unique DB constraint, but concurrent requests may receive a conflict instead of an existing certificate.
- Gateway is custom WebClient proxy rather than Spring Cloud Gateway; inspect header/body handling, CORS, upload streaming, and trust boundaries.
- Test JWT claim role mapping against the exact Keycloak realm; internal shared-secret auth should be replaced by workload identity/mTLS.
- For a production PDF, bundle an approved Unicode font (not included) to render Albanian special characters; current PDF uses built-in Helvetica.

## Run (requires Docker, Java 25 in build images, network for Maven dependencies)
`cp .env.example .env` then fill secure secrets and run `docker compose up --build -d`.
Existing Postgres volumes do NOT automatically run init.sql again: manually create `medlearn_certificates` on upgrades, or use a controlled database provisioning migration.

## Verification status
Source inspection and static checks only. Java 25, Maven, Docker and full frontend integration are not available in this execution environment; no successful runtime claim.
