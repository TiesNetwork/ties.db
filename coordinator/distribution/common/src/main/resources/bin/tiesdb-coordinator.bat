@echo off
if "%OS%" == "Windows_NT" SETLOCAL

pushd %~dp0..
if NOT DEFINED COORDINATOR_HOME set COORDINATOR_HOME=%CD%
popd

rem In the legacy mode always load Elassandra
if NOT DEFINED COORDINATOR_MAIN set COORDINATOR_MAIN=com.tiesdb.TiesDB
if NOT DEFINED JAVA_HOME goto :err

REM -----------------------------------------------------------------------------
REM JVM Opts we'll use in legacy run or installation
set JAVA_OPTS=-ea^
 -javaagent:"%COORDINATOR_HOME%\lib\jamm-0.3.0.jar"^
 -Xms2G^
 -Xmx2G^
 -XX:+HeapDumpOnOutOfMemoryError^
 -XX:+UseParNewGC^
 -XX:+UseConcMarkSweepGC^
 -XX:+CMSParallelRemarkEnabled^
 -XX:SurvivorRatio=8^
 -XX:MaxTenuringThreshold=1^
 -XX:CMSInitiatingOccupancyFraction=75^
 -XX:+UseCMSInitiatingOccupancyOnly^
 -Dlogback.configurationFile=logback.xml^
 -Djava.library.path="%COORDINATOR_HOME%\lib\sigar-bin"^
 -Dcoordinator.jmx.local.port=7199
REM **** JMX REMOTE ACCESS SETTINGS SEE: https://wiki.apache.org/cassandra/JmxSecurity ***
REM -Dcom.sun.management.jmxremote.port=7199^
REM -Dcom.sun.management.jmxremote.ssl=false^
REM -Dcom.sun.management.jmxremote.authenticate=true^
REM -Dcom.sun.management.jmxremote.password.file=C:\jmxremote.password

REM ***** CLASSPATH library setting *****
REM Ensure that any user defined CLASSPATH variables are not used on startup
set CLASSPATH="%COORDINATOR_HOME%\conf"

REM For each jar in the COORDINATOR_HOME lib directory call append to build the CLASSPATH variable.
for %%i in ("%COORDINATOR_HOME%\lib\*.jar") do call :append "%%i"
goto okClasspath

:append
set CLASSPATH=%CLASSPATH%;%1
goto :eof

REM -----------------------------------------------------------------------------
:okClasspath

REM JSR223 - collect all JSR223 engines' jars
for /D %%P in ("%COORDINATOR_HOME%\lib\jsr223\*.*") do (
	for %%i in ("%%P\*.jar") do call :append "%%i"
)

REM JSR223/JRuby - set ruby lib directory
if EXIST "%COORDINATOR_HOME%\lib\jsr223\jruby\ruby" (
    set JAVA_OPTS=%JAVA_OPTS% "-Djruby.lib=%COORDINATOR_HOME%\lib\jsr223\jruby"
)
REM JSR223/JRuby - set ruby JNI libraries root directory
if EXIST "%COORDINATOR_HOME%\lib\jsr223\jruby\jni" (
    set JAVA_OPTS=%JAVA_OPTS% "-Djffi.boot.library.path=%COORDINATOR_HOME%\lib\jsr223\jruby\jni"
)
REM JSR223/Jython - set python.home system property
if EXIST "%COORDINATOR_HOME%\lib\jsr223\jython\jython.jar" (
    set JAVA_OPTS=%JAVA_OPTS% "-Dpython.home=%COORDINATOR_HOME%\lib\jsr223\jython"
)
REM JSR223/Scala - necessary system property
if EXIST "%COORDINATOR_HOME%\lib\jsr223\scala\scala-compiler.jar" (
    set JAVA_OPTS=%JAVA_OPTS% "-Dscala.usejavacp=true"
)

REM Include the build\classes\main directory (if they exist only!) so it works in development
set COORDINATOR_CLASSPATH=%CLASSPATH%
if exist "%COORDINATOR_HOME%\build\classes\main" (
	set COORDINATOR_CLASSPATH=%COORDINATOR_CLASSPATH%;"%COORDINATOR_HOME%\build\classes\main"
)
if exist "%COORDINATOR_HOME%\build\classes\thrift" (
	set COORDINATOR_CLASSPATH=%COORDINATOR_CLASSPATH%;"%COORDINATOR_HOME%\build\classes\thrift"
)
set COORDINATOR_PARAMS=-Dcoordinator -Dcoordinator-foreground=yes
set COORDINATOR_PARAMS=%COORDINATOR_PARAMS% -Dcoordinator.logdir="%COORDINATOR_HOME%\logs"

echo Starting TiesDB Coordinator Server
"%JAVA_HOME%\bin\java" %JAVA_OPTS% %COORDINATOR_PARAMS% -cp %COORDINATOR_CLASSPATH% "%COORDINATOR_MAIN%"
goto finally

REM -----------------------------------------------------------------------------
:err
echo JAVA_HOME environment variable must be set!
pause

REM -----------------------------------------------------------------------------
:finally

ENDLOCAL
