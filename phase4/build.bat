@echo off
echo ==============================================================
echo   VANISH - Phase 4 Build Script
echo ==============================================================

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0compile.ps1"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Build failed.
    pause
    exit /b %ERRORLEVEL%
)
