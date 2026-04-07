@echo off
title Online Learning Platform - Stop Services
color 0C

echo ============================================================
echo    Online Learning Platform - Stop Services
echo    (Docker and databases will NOT be stopped)
echo ============================================================
echo.

echo Stopping all Spring Boot services...
echo.

REM Kill each service cmd window by its unique title
for %%s in (auth-service api-gateway student-service course-service instructor-service enrollment-service quiz-service certificate-service) do (
    tasklist /V /FI "IMAGENAME eq cmd.exe" 2>nul | findstr /I "OLP__%%s" >nul 2>&1
    if not errorlevel 1 (
        echo    [STOPPING] %%s
        taskkill /F /FI "WINDOWTITLE eq OLP__%%s*" >nul 2>&1
    ) else (
        echo    [NOT RUNNING] %%s
    )
)

echo.
echo Killing any Java processes on service ports...
echo.

for %%p in (8080 8081 8082 8083 8084 8085 8086 8087) do (
    for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr "LISTENING" ^| findstr ":%%p "') do (
        if not "%%a"=="" if not "%%a"=="0" (
            echo    Killing PID %%a on port %%p
            taskkill /F /PID %%a >nul 2>&1
        )
    )
)

echo.
echo ============================================================
echo    All services stopped!
echo    MySQL Docker container is still running.
echo    Your databases are safe.
echo ============================================================
echo.
pause
