# MedLearn Phase 2 — implementation status

## Added
- `course-service` (Java 25 / Spring Boot 4.0.1), separate `medlearn_courses` PostgreSQL database, Flyway migration.
- Course and chapter bilingual `sq`/`en` fields, categories, specialties, instructor IDs, profession/discipline/degree/year eligibility rules, draft/published/archived states.
- Public published course catalog and syllabus; authenticated learning routes; eligibility and enrollment grant checks; sequential chapter locks and progress.
- Administrator course create/update, chapter create/update, status updates, course list.
- Open Academy self-enrollment via enrollment service after eligibility check.
- Admin resource upload (PDF, MP4, PNG), MIME signature/size checks, private download route with course authorization.
- Next.js course catalog and learner course pages with persisted language selection.
- Unit test cases for locale fallback and eligibility matching.

## Not yet implemented / not production ready
- Chapter completion is currently an explicit user action; **there is no chapter quiz or 70% pass verification yet**. Phase 3 must make progress unlock conditional on a verified quiz result.
- Video streaming/range requests, image optimization, malware scanning, media transcoding, signed URLs, S3 integration, CDN and per-resource localization are not implemented.
- Instructor write permissions, full course editor UI, rich text sanitization, course versioning and audit logs remain incomplete.
- Registration email verification, SMTP, automatic notifications, payment verification with actual bank data, course purchase workflow, and admin bootstrap must be tested and completed.
- Keycloak service-account role mappings and issuer URLs need an actual container-based integration run; no full integration tests have passed in this environment.
- Enrollment grant must be made transactionally tied to approved professional payment records; currently an administrator can manually grant access.
- RabbitMQ is provisioned but no durable outbox or event consumers are implemented.
- No claim of CME/CPD accreditation.

## Security notes
- Authorization for protected content is enforced in the course service, not only the frontend.
- Users cannot select arbitrary IDs to mark another learner's progress; identity comes from JWT `sub`.
- Course and chapter payloads need validation constraints, HTML sanitization and server-side pagination before deployment.
- Sensitive fields in member profiles and document access require a dedicated privacy/least-privilege review.
- The development Docker Compose file must not be deployed as-is to the public internet.
