@rem Gradle startup script for Windows

@if "%DEBUG%" == "" @echo off
@rem Set local scope
setlocal
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0

set CLASSPATH=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

%JAVA_EXE% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %CMD_LINE_ARGS%
