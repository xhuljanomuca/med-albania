# Phase 4.2 — Validation report

## Scope
Continued from the Phase 4.1 archive. No new business feature phase introduced.

## Actual corrections
- Fixed `/api/courses` gateway destination selection (previously routed to enrollment service).
- Added gateway deny rule for internal paths (defense in depth; NOT a substitute for service authentication).
- Pinned frontend package versions in package.json (no lockfile generated because registry unavailable).
- Added GitHub Actions matrix for six Java 25 services, frontend build, and structure checks.
- Added gateway route regression test and manual end-to-end checklist.

## Environment constraints
- Available JDK: 21, required JDK: 25.
- Maven and Docker executables absent.
- npm registry DNS unavailable (EAI_AGAIN), so npm lockfile generation and frontend build cannot run.
- Maven Wrapper binary jar not generated; never fabricate a wrapper or lockfile.
- No database, Keycloak, Testcontainers, or end-to-end run was possible.

## Critical unresolved issues
- Full Java 25 compilation and dependency resolution remain unverified.
- Custom gateway proxy should be replaced by supported Spring Cloud Gateway implementation and tested with streaming/multipart payloads.
- Service-to-service shared-secret authentication needs replacement with short-lived scoped credentials/mTLS.
- Keycloak service account privileges and user provisioning need live verification.
- Flyway migrations require live PostgreSQL validation.
- Examination unlocks and certificate issuance require concurrency and authorization integration tests.
- Certificate issuance must be proven idempotent and identity/metadata provenance enforced.
- Frontend package-lock.json and Maven Wrapper remain missing.

## Status
NOT VERIFIED RUNNABLE. Do not deploy to production.
