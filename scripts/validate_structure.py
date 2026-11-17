#!/usr/bin/env python3
"""Dependency-free static checks; does not claim Java/Docker integration."""
from pathlib import Path
import json,xml.etree.ElementTree as ET,sys
root=Path(__file__).resolve().parents[1]
errors=[]; checks=0
def check(ok,msg):
 global checks
 checks+=1
 if not ok:errors.append(msg)
for f in root.rglob('pom.xml'):
 try:
  t=ET.parse(f);ns={'m':'http://maven.apache.org/POM/4.0.0'}
  check(t.findtext('.//m:java.version',namespaces=ns)=='25',f'{f}: Java 25')
 except Exception as e:errors.append(f'{f}: {e}')
for f in root.rglob('*.json'):
 try:json.loads(f.read_text());check(True,f'{f} json')
 except Exception as e:errors.append(f'{f}: {e}')
try:
 import yaml
 for f in list(root.rglob('*.yml'))+list(root.rglob('*.yaml')):
  yaml.safe_load(f.read_text());check(True,f'{f} yaml')
except ImportError:print('SKIP: YAML parsing (PyYAML not installed)')
cert=(root/'services/certificate-service/src/main/java/org/medlearn/cert/CertificateController.java').read_text()
check('record IssueRequest(UUID attemptId)' in cert,'certificate issuance must not accept user supplied name/title')
check('trustedName' in cert and 'trustedSq' in cert,'trusted metadata must be retrieved')
check('NOT_IMPLEMENTED' in cert,'unverified CME accreditation must be disabled')
exam=(root/'services/exam-service/src/main/java/org/medlearn/exam/ExamController.java').read_text()
check('"/internal/courses/"' in exam and '"/internal/chapters/"' in exam,'exam internal routes')
check('"/api/courses/internal/' not in exam,'old incorrect internal routes')
gw=(root/'services/api-gateway/src/main/resources/application.yml').read_text()
check('Path=/api/courses,/api/courses/**' in gw,'catalog gateway route')
check('RemoveRequestHeader=X-Internal-Secret' in gw,'gateway strips internal credential')
print(f'{checks-len(errors)}/{checks} static checks passed')
for e in errors:print('FAIL:',e)
sys.exit(bool(errors))
