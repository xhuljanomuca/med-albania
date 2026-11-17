# Manual end-to-end validation (NOT EXECUTED)

1. Start Docker Compose with unique local secrets; inspect all service health and logs.
2. Login via Keycloak with PKCE, verify issuer, audience and realm roles.
3. Confirm anonymous catalog GET /api/courses reaches course-service, not enrollment-service.
4. Register student and professional; reject duplicate national ID and unsupported PDF payloads.
5. Upload a valid PDF; verify admin approval cannot be performed by a learner.
6. Approve professional payment; verify course-specific grant, reject other courses.
7. Create bilingual course and ordered chapters; verify unpublished content is inaccessible.
8. Submit quiz with 69% and 70%; verify grading, attempt limits and chapter unlocks.
9. Verify final exam remains locked until mandatory chapters pass.
10. Issue certificate only for authenticated passed final exam; verify ownership, unique ID and QR PDF.
11. Attempt forged recipient/title, concurrent issuance, revoked certificate, expired certificate.
12. Verify all screens in sq/en and language persistence after refresh.
13. Verify direct calls to internal APIs are blocked at gateway and require service credentials.

All steps require execution and evidence in an environment with Docker and Java 25.
