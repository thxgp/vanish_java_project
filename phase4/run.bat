@echo off
cd /d "%~dp0"
echo ==============================================================
echo   VANISH - Starting Standalone Web Server
echo ==============================================================

if not exist bin\com\vanish\server\VanishServer.class (
    echo [INFO] Binaries not found. Running build.bat first...
    call build.bat
)

echo Starting Vanish Server on http://localhost:8080/ ...
start "" http://localhost:8080/
java -cp "web\WEB-INF\lib\javax.servlet-api-4.0.1.jar;bin" com.vanish.server.VanishServer 8080
pause
