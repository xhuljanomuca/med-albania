from pathlib import Path
r=Path(__file__).resolve().parents[1]
controller=(r/'services/enrollment-service/src/main/java/org/medlearn/EnrollmentController.java').read_text()
assert '@GetMapping("/me/history")' in controller
assert 'WHERE user_id=? ORDER BY occurred_at DESC,id DESC LIMIT 100' in controller
assert '@GetMapping("/me/summary")' in controller
assert (r/'services/enrollment-service/src/main/resources/db/migration/V4__audit_history_indexes.sql').is_file()
assert (r/'web/app/dashboard/history/page.tsx').is_file()
workflow=(r/'.github/workflows/verify.yml').read_text()
assert 'Keycloak discovery and realm availability' in workflow
print('Phase 15 source contracts: PASS')
