# Architecture decisions

- Browser uses OIDC authorization code with PKCE, access token in memory (no localStorage tokens); Keycloak is the identity provider.
- Gateway forwards bearer tokens; each downstream service validates issuer and signature independently.
- User service owns profiles, applications and payment document metadata. Enrollment service owns course grants. Separate databases.
- Roles: platform_admin, instructor, professional, student. Open Academy students/professionals are assigned their role, professional academy applicants remain disabled pending review.
- Java 25, Spring Boot 4, Spring Cloud 2025.1 baseline; verify exact compatibility before production.
- Keycloak's public issuer is `http://localhost:8081/realms/medlearn`. Containers must resolve the host; compose adds `host.docker.internal` mapping for Linux when needed (see gateway and services Dockerfile runtime guidance). For fully portable deployment use a single routable HTTPS issuer.
- The import JSON contains a **development placeholder** for backend client secret. The Compose startup copies the import template and substitutes the `.env` secret before the initial realm import. Imports are not reapplied to an existing realm.
- RabbitMQ is provisioned but not yet used for reliable event handling; future phase will add an outbox.
- Never use the mock screen counts as production data.
