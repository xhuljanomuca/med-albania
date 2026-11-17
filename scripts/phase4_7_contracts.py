#!/usr/bin/env python3
from pathlib import Path
import json,xml.etree.ElementTree as ET
import sys
root=Path(__file__).resolve().parents[1]
checks={}
def check(name,ok):
 checks[name]="PASS" if ok else "FAIL"
 return ok
for p in sorted(root.glob("services/*/pom.xml")):
 try: ET.parse(p);check("pom:"+p.parent.name,True)
 except ET.ParseError:check("pom:"+p.parent.name,False)
for p in sorted(root.glob("services/*/src/main/resources/application.yml")):
 check("config:"+p.parents[3].name,p.stat().st_size>0)
gateway=(root/"services/api-gateway/src/main/java/org/medlearn/GatewaySecurity.java").read_text()
check("registration only POST public",'.pathMatchers(org.springframework.http.HttpMethod.POST,"/api/users/register").permitAll()' in gateway)
check("internal paths denied",'.pathMatchers("/api/**/internal/**","/internal/**","/api/courses/internal/**").denyAll()' in gateway)
cert=(root/"services/certificate-service/src/main/java/org/medlearn/cert/CertificateController.java").read_text()
check("certificate transaction lock",'pg_advisory_xact_lock' in cert and '@Transactional' in cert)
check("certificate trusted metadata",'certificate-recipient' in cert and 'certificate-metadata' in cert)
check("testcontainers test",(root/"services/certificate-service/src/test/java/org/medlearn/cert/CertificateLockIntegrationTest.java").exists())
check("language resources",(root/"web/lib/i18n.ts").exists())
print(json.dumps(checks,indent=2))
sys.exit(0 if all(v=="PASS" for v in checks.values()) else 1)
