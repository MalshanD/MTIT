@echo off
title Online Learning Platform - Launcher
color 0A

echo ============================================================
echo    Online Learning Platform - Service Startup Script
echo ============================================================
echo.

REM Navigate to project root
cd /d "%~dp0"

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running! Please start Docker Desktop first.
    pause
    exit /b 1
)

echo [1/9] Starting MySQL Docker Container...
docker-compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start Docker container!
    pause
    exit /b 1
)
echo [OK] MySQL container started.
echo.

REM Wait for MySQL to be healthy
echo [2/9] Waiting for MySQL to be ready...
:wait_mysql
docker-compose exec -T mysql mysqladmin ping -h localhost -u root -proot >nul 2>&1
if %errorlevel% neq 0 (
    echo        Still waiting for MySQL...
    timeout /t 3 /nobreak >nul
    goto wait_mysql
)
echo [OK] MySQL is ready!
echo.

echo [3/9] Starting Auth Service (port 8081)...
start "OLP__auth-service" /min cmd /k "cd /d %~dp0auth-service && mvn spring-boot:run"
echo [OK] Auth Service window opened.
echo.

echo        Waiting for Auth Service to be ready...
:wait_auth
timeout /t 3 /nobreak >nul
netstat -ano | findstr "LISTENING" | findstr ":8081 " >nul 2>&1
if %errorlevel% neq 0 goto wait_auth
echo [OK] Auth Service is listening on port 8081!
echo.

echo [4/9] Starting API Gateway (port 8080)...
start "OLP__api-gateway" /min cmd /k "cd /d %~dp0api-gateway && mvn spring-boot:run"
echo [OK] API Gateway window opened.
echo.

timeout /t 5 /nobreak >nul

echo [5/9] Starting Student Service (port 8082)...
start "OLP__student-service" /min cmd /k "cd /d %~dp0student-service && mvn spring-boot:run"
echo [OK] Student Service window opened.
echo.

echo [6/9] Starting Course Service (port 8083)...
start "OLP__course-service" /min cmd /k "cd /d %~dp0course-service && mvn spring-boot:run"
echo [OK] Course Service window opened.
echo.

echo [7/9] Starting Instructor Service (port 8084)...
start "OLP__instructor-service" /min cmd /k "cd /d %~dp0instructor-service && mvn spring-boot:run"
echo [OK] Instructor Service window opened.
echo.

echo [8/9] Starting Enrollment Service (port 8085)...
start "OLP__enrollment-service" /min cmd /k "cd /d %~dp0enrollment-service && mvn spring-boot:run"
echo [OK] Enrollment Service window opened.
echo.

echo [9/9] Starting Quiz and Certificate Services (ports 8086, 8087)...
start "OLP__quiz-service" /min cmd /k "cd /d %~dp0quiz-service && mvn spring-boot:run"
start "OLP__certificate-service" /min cmd /k "cd /d %~dp0certificate-service && mvn spring-boot:run"
echo [OK] Quiz and Certificate Service windows opened.
echo.

echo ============================================================
echo    All services are starting up!
echo ============================================================
echo.
echo    Each service runs in a minimized window (OLP__...).
echo.
echo    API Gateway:         http://localhost:8080
echo    Auth Service:        http://localhost:8081
echo    Student Service:     http://localhost:8082
echo    Course Service:      http://localhost:8083
echo    Instructor Service:  http://localhost:8084
echo    Enrollment Service:  http://localhost:8085
echo    Quiz Service:        http://localhost:8086
echo    Certificate Service: http://localhost:8087
echo.
echo    Swagger UI: http://localhost:808X/swagger-ui.html
echo.
echo    Run stop.bat to stop all services.
echo    (Docker and databases will NOT be affected by stop.bat)
echo ============================================================
echo.
pause
