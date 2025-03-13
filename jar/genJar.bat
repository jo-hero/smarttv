@echo off

del "%~dp0\jstv_parse.jar"
rd /s/q "%~dp0\Smali_classes"
rd /s/q "%~dp0\jar.smali_classes\smali\net\jo\parse"
rd /s/q "%~dp0\jar.smali_classes\smali\net\jo\common"

java -jar "%~dp0\3rd\baksmali-2.5.2.jar" d "%~dp0\..\parse-library\build\intermediates\dex\release\mergeDexRelease\out\classes.dex" -o "%~dp0\Smali_classes"

if not exist "%~dp0\jar.smali_classes\smali\net\jo\" md "%~dp0\jar.smali_classes\smali\net\jo\"
if not exist "%~dp0\jar.smali_classes\assets\" md "%~dp0\jar.smali_classes\assets\"

::java -Dfile.encoding=utf-8 -jar "%~dp0\3rd\oss.jar" "%~dp0\Smali_classes"
move "%~dp0\Smali_classes\net\jo\parse" "%~dp0\jar.smali_classes\smali\net\jo\"
move "%~dp0\Smali_classes\net\jo\common" "%~dp0\jar.smali_classes\smali\net\jo\"
::xcopy "%~dp0\config.json" "%~dp0\jar.smali_classes\assets\"

java -jar "%~dp0\3rd\apktool_2.4.1.jar" b "%~dp0\jar.smali_classes" -c

move "%~dp0\jar.smali_classes\dist\dex.jar" "%~dp0\jstv_parse.jar"

certUtil -hashfile "%~dp0\jstv_parse.jar" MD5 | find /i /v "md5" | find /i /v "certutil" > "%~dp0\jstv_parse.jar.md5"

rd /s/q "%~dp0\jar.smali_classes\smali\net\jo\parse"
rd /s/q "%~dp0\jar.smali_classes\smali\net\jo\common"
rd /s/q "%~dp0\jar.smali_classes\build"
rd /s/q "%~dp0\jar.smali_classes\smali"
rd /s/q "%~dp0\jar.smali_classes\dist"
rd /s/q "%~dp0\jar.smali_classes\assets"
rd /s/q "%~dp0\Smali_classes"