#!/usr/bin/env python3
from pathlib import Path
import re, sys
root=Path(__file__).resolve().parents[1]
controller=(root/'services/certificate-service/src/main/java/org/medlearn/cert/CertificateController.java').read_text()
entity=(root/'services/certificate-service/src/main/java/org/medlearn/cert/Certificate.java').read_text()
security=(root/'services/certificate-service/src/main/java/org/medlearn/cert/CertificateSecurity.java').read_text()
checks={
  'transactional issuance': '@Transactional public Certificate issue' in controller,
  'transaction-scoped postgres lock': 'pg_advisory_xact_lock' in controller,
  'lock before existing lookup': controller.index('pg_advisory_xact_lock') < controller.index('findByExamAttemptId'),
  'database uniqueness': 'unique=true) public UUID examAttemptId' in entity,
  'trusted recipient lookup': '/certificate-recipient' in controller,
  'trusted course lookup': '/certificate-metadata' in controller,
  'service bearer token': 'setBearerAuth(serviceTokens.token())' in controller,
  'admin role protected': 'hasRole("platform_admin")' in security,
  'no client-provided certificate identity': 'record IssueRequest(UUID attemptId)' in controller,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL')+' '+name)
sys.exit(0 if all(checks.values()) else 1)
