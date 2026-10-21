#!/usr/bin/env python3
"""Small structural/privacy regression checks; not a security audit."""
from pathlib import Path
import xml.etree.ElementTree as ET
r=Path(__file__).resolve().parents[1]
a='{http://schemas.android.com/apk/res/android}'
x=ET.parse(r/'app/src/main/AndroidManifest.xml').getroot()
permissions={p.get(a+'name') for p in x.findall('uses-permission')}
assert 'android.permission.INTERNET' not in permissions
assert 'android.permission.RECORD_AUDIO' not in permissions
assert 'android.permission.READ_PHONE_NUMBERS' not in permissions
app=x.find('application')
assert app.get(a+'allowBackup')=='false'
assert app.get(a+'usesCleartextTraffic')=='false'
service=app.find('service')
assert service.get(a+'permission')=='android.permission.BIND_INCALL_SERVICE'
assert service.get(a+'exported')=='true'
assert app.find('receiver').get(a+'exported')=='false'
assert 'minSdk = 29' in (r/'app/build.gradle.kts').read_text()
assert 'targetSdk = 36' in (r/'app/build.gradle.kts').read_text()
for p in r.rglob('*'):
    if any(part in ('.git','.gradle','build') for part in p.relative_to(r).parts): continue
    assert p.suffix not in ('.jks','.keystore','.p12'), f'Private signing material: {p.name}'
print('PASS: 10 manifest/configuration checks and no private signing files in project sources')
