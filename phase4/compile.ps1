$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $ScriptDir

if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
if (-not (Test-Path "web\WEB-INF\classes")) { New-Item -ItemType Directory -Path "web\WEB-INF\classes" | Out-Null }

Write-Host "[1/3] Compiling Java classes (Phase 2 core + Phase 4 web)..." -ForegroundColor Cyan

$sources = @()
Get-ChildItem -Recurse "..\phase2\final\src\*.java" | ForEach-Object { $sources += ('"' + $_.FullName.Replace('\', '/') + '"') }
Get-ChildItem -Recurse "src\*.java" | ForEach-Object { $sources += ('"' + $_.FullName.Replace('\', '/') + '"') }

$sourcesFile = Join-Path $ScriptDir "sources.txt"
[System.IO.File]::WriteAllLines($sourcesFile, $sources)

$cp = "web/WEB-INF/lib/javax.servlet-api-4.0.1.jar;../lib/javax.servlet-api-4.0.1.jar"
$javacArgs = @("--release", "17", "-cp", $cp, "-d", "bin", "@sources.txt")

& javac $javacArgs
$exitCode = $LASTEXITCODE
Remove-Item $sourcesFile -ErrorAction SilentlyContinue

if ($exitCode -ne 0) {
    Write-Error "Compilation failed with code $exitCode"
    exit $exitCode
}

Write-Host "[2/3] Syncing classes to web\WEB-INF\classes..." -ForegroundColor Cyan
Copy-Item -Recurse -Force "bin\*" "web\WEB-INF\classes\"

Write-Host "[3/3] Packaging vanish.war archive..." -ForegroundColor Cyan
Set-Location "web"
& jar -cvf "..\vanish.war" * | Out-Null
Set-Location $ScriptDir

Write-Host "`nBuild SUCCESS! WAR Archive created at: phase4\vanish.war" -ForegroundColor Green
