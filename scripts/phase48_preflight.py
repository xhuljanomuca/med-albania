#!/usr/bin/env python3
"""Offline MedLearn preflight. Static checks are NOT compilation or integration tests."""
from pathlib import Path
import json, sys, xml.etree.ElementTree as ET
import yaml
root=Path(__file__).resolve().parents[1]
errors=[]; checked=0
for path in sorted(root.rglob('pom.xml')):
    try:
        doc=ET.parse(path); checked+=1
        ns={'m':'http://maven.apache.org/POM/4.0.0'}
        version=doc.findtext('.//m:properties/m:java.version',namespaces=ns)
        if version!='25': errors.append(f'{path}: expected Java 25, got {version}')
    except Exception as e: errors.append(f'{path}: {e}')
for path in sorted(root.rglob('*.json')):
    if 'node_modules' in path.parts: continue
    try: json.loads(path.read_text()); checked+=1
    except Exception as e: errors.append(f'{path}: {e}')
for ext in ('*.yaml','*.yml'):
    for path in sorted(root.rglob(ext)):
        try: yaml.safe_load(path.read_text()); checked+=1
        except Exception as e: errors.append(f'{path}: {e}')
services=list((root/'services').glob('*/pom.xml'))
if len(services)!=6: errors.append(f'expected 6 Maven services, found {len(services)}')
if not (root/'web/package-lock.json').exists(): print('WARNING: npm lockfile missing; frontend build is not reproducible')
if not (root/'.mvn/wrapper/maven-wrapper.jar').exists(): print('WARNING: Maven Wrapper not present')
print(f'Static checks: {checked} configuration files parsed; {len(errors)} errors')
for error in errors: print('ERROR:',error)
sys.exit(1 if errors else 0)
