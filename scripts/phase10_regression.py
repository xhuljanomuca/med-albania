from pathlib import Path
r=Path(__file__).resolve().parents[1]
w=(r/'services/user-service/src/main/java/org/medlearn/KeycloakEnableWorker.java').read_text()
assert 'dead_lettered_at IS NULL' in w and 'claim_token=?' in w and 'requeue(UUID id)' in w
assert 'dead_lettered_at TIMESTAMPTZ' in (r/'services/user-service/src/main/resources/db/migration/V5__keycloak_enable_dead_letter.sql').read_text()
c=(r/'services/course-service/src/main/java/org/medlearn/CourseController.java').read_text()
assert 'adminChapters(' in c and 'checkChapterPosition(' in c
assert (r/'web/app/admin/approval-sync/page.tsx').exists()
assert (r/'web/app/admin/courses/[id]/chapters/page.tsx').exists()
print('Phase 10 source contracts passed (not compilation or runtime tests)')
