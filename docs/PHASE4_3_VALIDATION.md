# MedLearn Phase 4.3 — Validation and blockers

## Source changes
- Removed byte-buffering WebClient proxy in favor of Spring Cloud Gateway Server WebFlux routing (Spring Cloud BOM 2025.1.0; dependency compatibility NOT verified).
- Added explicit gateway route predicates, including the exact `/api/courses` path.
- Removed client-supplied `X-Internal-Secret` and `X-Forwarded-Client-Cert` headers at the gateway.
- Placed internal path denial before administrator matching.
- Changed several internal endpoints from anonymous access to requiring authentication. **This intentionally fails closed but can break internal calls until service OAuth credentials are wired.**

## Tests and limits
- Configuration and source static checks: see `reports/static-validation.json`.
- Java 25, Maven, Docker and PostgreSQL unavailable here: compilation, migrations, Keycloak integration and E2E tests NOT RUN.
- npm registry connectivity and frontend lockfile availability need independent verification.
- **NOT production ready.** No end-to-end evidence of correct service-to-service OAuth client credentials, transactional unlocking, or certificate issuance.

## Security blockers
1. Replace all `X-Internal-Secret` calls with scoped OAuth2 client credentials, validate audience and `service_*` roles in downstream services. Current service callers are NOT migrated.
2. Make certificate issuance idempotent under concurrent requests by enforcing unique exam attempt constraint, catching uniqueness violations and fetching existing record. Add concurrency integration test.
3. Verify chapter unlocking in database transactions and add simultaneous submission tests.
4. Replace placeholder secrets, validate uploaded PDF content and authorization, review PII exposure.
5. Run full CI in environment with Java 25, Maven, Docker and registry access.
