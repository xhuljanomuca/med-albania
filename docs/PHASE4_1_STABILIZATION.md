# MedLearn Phase 4.1 — Stabilization report

Date: 2026-11-16. This is a **partial stabilization patch**, not a successful end-to-end build.

## Changes applied

1. Certificate issuance now accepts only `attemptId`. Recipient identity is fetched from the User Service by Keycloak subject, and Albanian/English course titles are fetched from the Course Service by the course ID attested by the Exam Service. The service rejects inactive profiles, unpublished courses and mismatched IDs. User-supplied recipient/course labels are no longer accepted.
2. Added internal profile and course metadata endpoints with constant-time shared-secret checks. Updated Docker Compose to pass internal secret and service URLs. These endpoints are *not* exposed by the gateway.
3. Fixed the Course Service security matcher for `/internal/**` paths, previously inconsistent with actual controller mappings.
4. Fixed Exam Service's internal Course Service URLs, previously incorrectly prefixed with `/api/courses/internal/`.
5. Gateway routing now handles the exact `/api/courses` catalog path, strips incoming `X-Internal-Secret`, and excludes hop-by-hop response headers. Its proxy implementation is **not yet integration-verified**; a standard Spring Cloud Gateway proxy is recommended before deployment.
6. Disabled admin-controlled CME/CPD accreditation updates until a trustworthy accreditation authority and evidence approval process exists. This endpoint now returns HTTP 501.
7. Added repeatable static validation script `scripts/validate_structure.py`.

## Tests actually executed

- `python scripts/validate_structure.py`: **23/23 static assertions passed** (XML, JSON, YAML syntax, selected security/route invariants).
- `javac` (OpenJDK 21) compilation of `CertificatePolicy.java`: **passed**.
- Four isolated Java policy assertions using OpenJDK 21: **4/4 passed**.
- ZIP archive integrity: validated after packaging.

## Tests NOT executed

- Java 25 compilation and all Maven/Spring Boot unit/integration tests: JDK 25 and Maven not installed in this environment.
- Docker Compose build/start, Keycloak login/registration, RabbitMQ, PostgreSQL/Flyway migrations: Docker daemon/CLI unavailable.
- Next.js build and browser workflows: dependencies were not installed; npm build was not executed.
- Real authenticated certificate issuance, course enrollment, final examination, certificate PDF and QR verification end-to-end.

## Important unresolved defects and security work

- **Gateway**: the custom WebFlux reverse proxy requires a build and streaming/upload/error-path integration test; use the official Spring Cloud Gateway stack instead of considering this proxy production ready.
- **Authorization**: JWT issuer/Keycloak hostnames and service-to-service authentication require testing in Docker and behind HTTPS. Shared internal secret is only an interim control; migrate to mTLS or scoped OAuth2 client credentials.
- **Registration**: payment PDF upload checks magic bytes but not full PDF parsing/malware scanning; needs content inspection, encryption at rest, download authorization, rate limiting, CSRF strategy, audit trails and data retention controls.
- **Exams**: cross-service chapter completion is not transactional with the exam database; implement outbox/event delivery and idempotent retries. Attempt concurrency and answer leakage must be tested.
- **Certificates**: issuance may race on unique attempt ID; add concurrency-safe idempotency handling and test. Verification reveals a recipient name publicly; document consent and privacy controls.
- **Accreditation**: no verified accreditation authority or approval workflow is integrated; credits must not be represented as officially recognized.
- **Frontend**: verify localization, persistent language selection, OIDC PKCE callback behavior and protected routes with a real browser.
- **Database**: Flyway migrations were syntax-inspected only; no PostgreSQL instance was available to execute them.
- **Build reproducibility**: pin image digests and frontend dependency lockfile, add Maven Wrapper, CI and integration test environment.

## Local validation commands

```bash
python3 scripts/validate_structure.py
for d in services/*; do (cd "$d" && mvn -B clean verify) || exit 1; done
(cd web && npm ci && npm run build)
cp .env.example .env  # edit all secrets before starting
# docker compose config
# docker compose up --build -d
# docker compose ps
```

Do not mark Phase 4.1 complete until these builds and authenticated end-to-end tests pass.
