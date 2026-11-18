from pathlib import Path
import json, yaml
r=Path(__file__).resolve().parents[1]
realm=json.loads((r/'infra/keycloak/medlearn-realm.json').read_text())
for c in realm['clients']:
 assert any(m.get('config',{}).get('included.custom.audience')=='medlearn-api' for m in c['protocolMappers']),c['clientId']
for service in ('api-gateway','user-service','enrollment-service','course-service','exam-service','certificate-service'):
 p=r/'services'/service/'src/main/resources/application.yml'
 config=yaml.safe_load(p.read_text());assert config['medlearn']['security']['audience']=='${JWT_AUDIENCE:medlearn-api}',service
 assert list((r/'services'/service/'src/main/java').rglob('*AudienceConfig.java')),service
sql=(r/'services/exam-service/src/main/resources/db/migration/V2__progress_outbox.sql').read_text()
assert 'UNIQUE REFERENCES attempt(id)' in sql
controller=(r/'services/exam-service/src/main/java/org/medlearn/exam/ExamController.java').read_text()
assert 'outbox.save(event)' in controller and 'http.post().uri(courses+"/api/courses/internal/chapters/"+q.chapterId' not in controller
print('Phase 4.11 source contracts: PASS (static only)')
