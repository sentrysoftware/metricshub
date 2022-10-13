@echo off
@REM Licensed under the Sentry Free Software License Agreement.
@REM See the LICENSE file distributed with this work for additional
@REM information regarding copyright ownership.
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

@REM -----------------------------------------------------------------------------
@REM Hardware Sentry Agent Startup Script
@REM
@REM Environment Variable Prerequisites
@REM
@REM   JAVA_HOME       Must point at a Java Runtime Environment, version 11 or
@REM                   greater.
@REM   HWS_JAVA_OPTS   (Optional) Java runtime options used when Hardware Sentry
@REM                   Agent is executed.
@REM -----------------------------------------------------------------------------

@setlocal

set ERROR_CODE=0

@REM Check for JAVA_HOME
if not "%JAVA_HOME%"=="" goto OkJHome
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
goto checkJCmd

:OkJHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJCmd
if exist "%JAVACMD%" goto chkHwsHome

echo The JAVA_HOME environment variable is not defined correctly. >&2
echo JAVA_HOME must point to a JRE version 11 or greater. >&2
goto error

:chkHwsHome
set "HWS_HOME=%~dp0.."
if not "%HWS_HOME%"=="" goto stripHwsHome
goto error

:stripHwsHome
if not "_%HWS_HOME:~-1%"=="_\" goto checkHwsCmd
set "HWS_HOME=%HWS_HOME:~0,-1%"
goto stripHwsHome

:checkHwsCmd
if exist "%HWS_HOME%\bin\hws-agent.cmd" goto init
goto error

@REM Ready to execute
:init

set HWS_JAR=%HWS_HOME%\lib\${project.artifactId}-${project.version}.jar

@REM Option that enables traces and metrics exports using the OTel agent
set HWS_AUTO_INST=-javaagent:"%HWS_HOME%\otel\opentelemetry-javaagent.jar" -Dotel.resource.attributes=service.name=Hardware-Sentry-Agent -Dotel.traces.exporter=otlp -Dotel.metrics.exporter=otlp -Dotel.exporter.otlp.endpoint=https://localhost:4317 -Dotel.exporter.otlp.certificate="%HWS_HOME%\security\otel.crt" -Dotel.exporter.otlp.headers="Authorization=Basic aHdzOlNlbnRyeVNvZnR3YXJlMSE="

@REM Auto-instrumentation option is enabled by default
set ARGS=%*
if "%1"=="" goto noArgs
if not x%ARGS:--no-auto-instrumentation=%==x%ARGS% echo Auto-instrumentation is disabled && set "HWS_AUTO_INST="
:noArgs

set "HWS_JAVA_OPTS=%HWS_JAVA_OPTS% %HWS_AUTO_INST%"

"%JAVACMD%" %HWS_JAVA_OPTS% -jar "%HWS_JAR%" %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

cmd /C exit /B %ERROR_CODE%
