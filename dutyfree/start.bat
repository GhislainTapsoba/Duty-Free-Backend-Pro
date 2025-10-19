@echo off
echo ========================================
echo   DEMARRAGE DUTY FREE BACKEND
echo ========================================
echo.
echo Demarrage des services Docker...
docker-compose up -d

echo.
echo Attente du demarrage (30 secondes)...
timeout /t 30 /nobreak > nul

echo.
echo Verification de la sante...
curl -s http://localhost:8080/actuator/health

echo.
echo ========================================
echo   APPLICATION DEMARREE!
echo ========================================
echo.
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.
pause