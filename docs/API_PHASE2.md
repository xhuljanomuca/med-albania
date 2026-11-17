# Course API (via gateway `http://localhost:8080`)

| Method | Path | Authorization | Description |
|---|---|---|---|
| GET | `/api/courses` | Public | Published bilingual catalog; `Accept-Language: sq/en` |
| GET | `/api/courses/{id}` | Public | Published course and syllabus |
| GET | `/api/courses/{id}/eligibility` | Authenticated | Check eligibility for self-enrollment |
| POST | `/api/enrollments/open/{courseId}` | Authenticated | Self-enroll in eligible Open Academy course |
| GET | `/api/courses/{id}/learn` | Authenticated + active eligible grant | Chapter progress and locks |
| GET | `/api/courses/chapters/{chapterId}` | Authenticated + eligible grant + prerequisites | Chapter body |
| POST | `/api/courses/chapters/{chapterId}/complete` | Authenticated + eligible grant + prerequisites | Mark chapter complete (temporary Phase 2 behavior) |
| GET | `/api/courses/resources/{id}` | Authenticated + eligible grant | Private media |
| GET | `/api/courses/admin/all` | platform_admin | All courses |
| POST | `/api/courses/admin` | platform_admin | Create draft course |
| PUT | `/api/courses/admin/{id}` | platform_admin | Update course |
| POST | `/api/courses/admin/{id}/chapters` | platform_admin | Add chapter |
| PUT | `/api/courses/admin/chapters/{id}` | platform_admin | Edit chapter |
| POST | `/api/courses/admin/{id}/status/{status}` | platform_admin | DRAFT/PUBLISHED/ARCHIVED |
| POST | `/api/courses/admin/chapters/{chapterId}/resources` | platform_admin | Multipart `file`, `downloadable` |

The `eligibleProfessions`, `eligibleDisciplines`, `eligibleDegrees`, `eligibleYears` fields are comma-separated values or `*` for any. They should be normalized into join tables in a later migration.
