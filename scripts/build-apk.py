#!/usr/bin/env python3
"""Dependency-free SDK build: Java 17+ and Android SDK 35/build-tools 35.0.0.
Uses a local DEVELOPMENT signing key; never distribute this key or use it for a production release.
"""
import os, pathlib, subprocess, shutil, zipfile
import xml.etree.ElementTree as ET
root=pathlib.Path(__file__).resolve().parents[1]
os.chdir(root)
sdk=pathlib.Path(os.environ.get('ANDROID_SDK_ROOT',os.environ.get('ANDROID_HOME','')))
bt=sdk/'build-tools'/'35.0.0'; android=sdk/'platforms'/'android-35'/'android.jar'
if not android.is_file() or not (bt/'aapt2').is_file():
    raise SystemExit('Set ANDROID_SDK_ROOT to SDK with platforms;android-35 and build-tools;35.0.0 installed.')
build=root/'build'/'sdk'
if build.exists(): shutil.rmtree(build)
for p in ['gen','classes','dex']: (build/p).mkdir(parents=True,exist_ok=True)
def run(*args): subprocess.run([str(x) for x in args],check=True)
run('java','com.sun.tools.javac.Main','-version')
run(bt/'aapt2','compile','--dir','app/src/main/res','-o',build/'resources.zip')
manifest=ET.parse('app/src/main/AndroidManifest.xml')
manifest.getroot().set('package','com.kvd.bubblelevel')
manifest.write(build/'AndroidManifest.xml',encoding='utf-8',xml_declaration=True)
run(bt/'aapt2','link','-o',build/'base.apk','-I',android,'--manifest',build/'AndroidManifest.xml','--java',build/'gen','--auto-add-overlay',build/'resources.zip')
sources=list((root/'app/src/main/java').rglob('*.java'))+list((build/'gen').rglob('*.java'))
run('java','com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-encoding','UTF-8','-bootclasspath',str(android)+os.pathsep+str(bt/'core-lambda-stubs.jar'),'-d',build/'classes',*sources)
classes=list((build/'classes').rglob('*.class'))
# D8 has no app dependencies to desugar; API 26 supports used Java library methods.
run('java','-cp',bt/'lib/d8.jar','com.android.tools.r8.D8','--lib',android,'--min-api','26','--output',build/'dex',*classes)
with zipfile.ZipFile(build/'base.apk','a',zipfile.ZIP_DEFLATED) as apk:
    for file in (build/'dex').glob('*.dex'): apk.write(file,file.name)
run(bt/'zipalign','-f','-p','4',build/'base.apk',build/'aligned.apk')
key=pathlib.Path(os.environ.get('LEVEL_DEBUG_KEY',str(pathlib.Path.home()/'.android'/'kvd-level-debug.keystore')))
key.parent.mkdir(parents=True,exist_ok=True)
if not key.exists():
    run('keytool','-genkeypair','-keystore',key,'-storepass','android','-keypass','android','-alias','androiddebugkey','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Android Debug,O=KVD Development,C=US')
out=root/'build'/'Uroven-1.2.0.apk'
signed=build/'signed.apk'
run('java','-jar',bt/'lib/apksigner.jar','sign','--ks',key,'--ks-pass','pass:android','--key-pass','pass:android','--out',signed,build/'aligned.apk')
run('java','-jar',bt/'lib/apksigner.jar','verify','--verbose',signed)
with zipfile.ZipFile(signed) as apk:
    if apk.testzip() is not None: raise SystemExit('Corrupt APK entry')
os.replace(signed,out)
print(out)
