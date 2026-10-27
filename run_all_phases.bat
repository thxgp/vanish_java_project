@echo off
title Vanish Project - Multi-Phase Launcher
cls
echo ==============================================================
echo    VANISH - ONE-TIME SECRET NOTE SHARING APPLICATION
echo    Comprehensive 4-Phase Academic Deliverable
echo ==============================================================
echo.
echo Please select a Phase to run:
echo.
echo  [1] Phase 1 - Core Application Setup ^& Validation (Console Walkthrough)
echo  [2] Phase 2 - Structured Programming, Security ^& Cryptography (Console Walkthrough)
echo  [3] Phase 3 - Web Interface, HTML5/CSS3 Forms ^& JSON Data (Open in Browser)
echo  [4] Phase 4 - Full-Stack Server, Servlets, HttpSession ^& JSP (Launch Server)
echo  [5] Run Phase 4 Automated Test Suite (21 Verification Tests)
echo  [6] Re-build Phase 4 WAR Package (vanish.war)
echo  [0] Exit
echo.
set /p choice="Enter your choice (0-6): "

if "%choice%"=="1" (
    cls
    echo Running Phase 1 Walkthrough...
    cd phase1\final
    java VanishPhase1
    cd ..\..
    pause
    goto end
)

if "%choice%"=="2" (
    cls
    echo Running Phase 2 Walkthrough...
    cd phase2\final
    java -cp bin com.vanish.VanishPhase2
    cd ..\..
    pause
    goto end
)

if "%choice%"=="3" (
    cls
    echo Opening Phase 3 Web Interface in Browser...
    start "" "phase3\index.html"
    goto end
)

if "%choice%"=="4" (
    cls
    echo Launching Phase 4 Embedded Server...
    cd phase4
    call run.bat
    cd ..
    goto end
)

if "%choice%"=="5" (
    cls
    echo Running Phase 4 Automated Test Suite...
    cd phase4
    java -cp "web/WEB-INF/lib/javax.servlet-api-4.0.1.jar;bin" com.vanish.test.Phase4Test
    cd ..
    pause
    goto end
)

if "%choice%"=="6" (
    cls
    echo Rebuilding Phase 4 WAR...
    cd phase4
    call build.bat
    cd ..
    pause
    goto end
)

:end
