# MedLearn — Phase 1

Java 25 / Spring Boot 4 microservices foundation for a bilingual medical education platform.

## What is implemented
- Keycloak OAuth2/OIDC with JWT validation at the gateway and services.
- Public registration for **Professional Academy** (disabled in Keycloak until administrator approval) and **Open Academy** (enabled immediately).
- Profession, discipline, degree, year, university and profile data; profession/student choices validated server-side.
- PDF payment evidence submitted with professional registration, stored outside the public web root with a random filename, maximum 5 MiB, PDF signature check; no public download route.
- Admin approval/rejection, course grants and access checks. Role claims are read from `realm_access.roles`.
- Bilingual Next.js registration/login/member/admin layouts and translations; language persists in localStorage.
- Separate PostgreSQL databases for user and enrollment services, Flyway migrations.
- Gateway, RabbitMQ container and initial event publishing for registration/approval (event consumption is deferred).

## Requirements
Docker Engine with Compose v2. For running services outside containers: Java 25, Maven 3.9+, Node 22+.

## Run locally
1. `cp .env.example .env` and set **new strong secrets**. Never commit `.env`.
2. `docker compose up --build -d`
3. Open `http://localhost:3000` (frontend), `http://localhost:8080` (gateway), Keycloak at `http://localhost:8081`.
4. Log in with the Keycloak administrator configured by `KC_BOOTSTRAP_ADMIN_USERNAME` / `KC_BOOTSTRAP_ADMIN_PASSWORD`; create a **separate MedLearn realm admin** with role `platform_admin` for application administration.
5. For browser login, use the login button; the `medlearn-web` public OIDC client uses authorization-code + PKCE.

The Keycloak realm is imported from `infra/keycloak/medlearn-realm.json` on first boot. Its bootstrap client `medlearn-backend` uses a service account and realm-management roles to create/enable users; rotate its secret for real deployments. The realm has no pre-created platform administrator; provision one explicitly.

**Local development only:** The demo uses HTTP and local bind mounts. Production requires HTTPS, secrets management, antivirus scanning of uploads, rate limits, backups, key rotation, external object storage, hardened CORS/CSP, audit retention, verified email delivery and a security review. RabbitMQ is provisioned but the transactional outbox and consumers are future work.

## API (via gateway)
- `GET /api/users/catalog` public profession/degree/discipline choices
- `POST /api/users/register` multipart form: `payload` JSON string and optional `payment` PDF
- `GET /api/users/me` authenticated
- `PUT /api/users/me` authenticated
- `GET /api/users/admin/pending` platform_admin
- `POST /api/users/admin/{id}/decision` platform_admin, JSON `{ "approve": true, "reason": "..." }`
- `GET /api/enrollments/me` authenticated
- `GET /api/enrollments/access/{courseId}` authenticated
- `POST /api/enrollments/admin/grants` platform_admin, JSON `{ "userId":"...", "courseId":"..." }`

## Source layout
`services/user-service`, `services/enrollment-service`, `services/api-gateway`, `web`, `infra/keycloak`, `docs`.

## Security notes and limitations
- Public registration creates a user in Keycloak through a server-side service-account API. Passwords never enter the platform database; they are sent to Keycloak only over the configured network. Enable TLS in production.
- Professional applicants are **disabled** until approved; their PDF is uploaded in the initial multipart registration.
- Open Academy users are immediately enabled, but course grants are still explicit; degree eligibility is groundwork only, not a full catalog or enrollment engine.
- The admin grants endpoint requires `platform_admin`; it must not be exposed through a frontend-only check.
- Keycloak users and application records are created across separate systems; compensation deletes Keycloak users if persistence fails. This is not a distributed transaction.
- Email verification is enabled in the realm, but SMTP must be configured before verification email delivery works.
- Payment evidence is **not payment verification**. Admin approval is a manual decision.
- This code is a foundation and has **not been executed end-to-end in this environment** (Java 25, Maven and Docker are unavailable here). Build and integration testing are required before deployment.

## Tests
`mvn -f services/user-service/pom.xml test`; `mvn -f services/enrollment-service/pom.xml test`; `cd web && npm install && npm run build`.

## Next phases
1. Courses, translations, chapters, instructor assignments and eligibility policies.
2. Quizzes, attempts, 70% passing, progression locks and final exams.
3. Certificates, accreditation (verified only), payment workflows, reporting.
4. Announcements, community, notifications, support, audit, webinars and observability.


## Phase 2
See [Phase 2 status](docs/PHASE2.md) and [Phase 2 API](docs/API_PHASE2.md). Course service is port 8084 inside Docker; gateway exposes `/api/courses/**`. Run `docker compose up --build -d` after setting `.env`.

## Phase 4 additions
See `docs/PHASE4.md`. Certificate service, PDF/QR verification, achievement record and basic certificate gallery/admin page have been added.
