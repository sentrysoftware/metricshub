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

@REM ════════════════════════════════════════════════════════════════════════════
@REM ╦ ╦┌─┐┬─┐┌┬┐┬ ┬┌─┐┬─┐┌─┐  ╔═╗┌─┐┌┐┌┌┬┐┬─┐┬ ┬
@REM ╠═╣├─┤├┬┘ │││││├─┤├┬┘├┤   ╚═╗├┤ │││ │ ├┬┘└┬┘
@REM ╩ ╩┴ ┴┴└──┴┘└┴┘┴ ┴┴└─└─┘  ╚═╝└─┘┘└┘ ┴ ┴└─ ┴
@REM
@REM Hardware Sentry OpenTelemetry Collector Startup Script
@REM
@REM Version ${project.version}
@REM Build ${buildNumber} on ${timestamp}
@REM ════════════════════════════════════════════════════════════════════════════

@setlocal

set ERROR_CODE=0

:chkHwsHome
set "HWS_HOME=%~dp0.."
if not "%HWS_HOME%"=="" goto stripHwsHome
goto error

:stripHwsHome
if not "_%HWS_HOME:~-1%"=="_\" goto checkHwsCmd
set "HWS_HOME=%HWS_HOME:~0,-1%"
goto stripHwsHome

:checkHwsCmd
if exist "%HWS_HOME%\bin\hws-otel.cmd" goto init
goto error

:init
if not "%1"=="" goto exec
set "DEFAULT_ARGS=--config config\otel-config.yaml"

:exec
@pushd "%HWS_HOME%"
bin\${project.artifactId}.exe %DEFAULT_ARGS% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@popd
@endlocal & set ERROR_CODE=%ERROR_CODE%

cmd /C exit /B %ERROR_CODE%
