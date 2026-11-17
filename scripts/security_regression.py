#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
checks={
 'user internal role': ('services/user-service/src/main/java/org/medlearn/SecurityConfig.java','.requestMatchers("/internal/profiles/**").hasRole("svc_certificate")'),
 'course completion service role': ('services/course-service/src/main/java/org/medlearn/SecurityConfig.java','.requestMatchers("/internal/chapters/**").hasRole("svc_exam")'),
 'course internal fail closed': ('services/course-service/src/main/java/org/medlearn/SecurityConfig.java','.requestMatchers("/internal/**").denyAll()'),
 'gateway internal denial': ('services/api-gateway/src/main/java/org/medlearn/GatewaySecurity.java','.pathMatchers("/api/**/internal/**","/internal/**","/api/courses/internal/**").denyAll()'),
 'exam certificate role': ('services/exam-service/src/main/java/org/medlearn/exam/ExamSecurity.java','.requestMatchers("/internal/exams/**").hasRole("svc_certificate")'),
 'certificate admin role': ('services/certificate-service/src/main/java/org/medlearn/cert/CertificateSecurity.java','.requestMatchers("/api/certificates/admin/**").hasRole("platform_admin")'),
}
for name,(p,needle) in checks.items():
 assert needle in (root/p).read_text(),name
 print('PASS:',name)
for p in ['services/course-service/src/main/java/org/medlearn/SecurityConfig.java','services/api-gateway/src/main/java/org/medlearn/GatewaySecurity.java']:
 assert '"/api/courses","/api/courses/*").permitAll()' not in (root/p).read_text()
print('PASS: no unrestricted course wildcard')
