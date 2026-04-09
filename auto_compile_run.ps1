# Robust auto compile + run script (fixed classpath separator and absolute paths)
# Usage: powershell -ExecutionPolicy Bypass -File .\auto_compile_run.ps1

# Move to script directory so relative paths are predictable
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $ScriptDir

$src = "gect-connect"
$bin = Join-Path $src "bin"

# Robust OS check (works across PowerShell versions)
$isWindows = [System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform([System.Runtime.InteropServices.OSPlatform]::Windows)
$sep = if ($isWindows) { ';' } else { ':' }

function Find-Jars {
    param([string[]]$dirs)
    $jars = @()
    foreach ($d in $dirs) {
        if (Test-Path $d) {
            $jars += Get-ChildItem -Path $d -Filter *.jar -File -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
        }
    }
    return $jars
}

# Candidate jar directories
$jarDirs = @(
    $ScriptDir,
    (Join-Path $ScriptDir ".."),
    (Join-Path $ScriptDir "lib"),
    (Join-Path $ScriptDir "libs"),
    (Join-Path $ScriptDir "gect-connect")
)

while ($true) {
    Clear-Host
    Write-Host " Rebuilding project..." -ForegroundColor Cyan

    # Kill previous Java processes
    Get-Process -Name java -ErrorAction SilentlyContinue | ForEach-Object {
        try { Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue } catch {}
    }

    # Clean + recreate bin
    if (Test-Path $bin) { Remove-Item -Recurse -Force $bin -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Path $bin | Out-Null

    # Find .java files
    $javaFiles = Get-ChildItem -Recurse -Path $src -Filter *.java -File -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
    if (-not $javaFiles -or $javaFiles.Count -eq 0) {
        Write-Host "❌ No Java source files found under '$src'. Make sure you run this from the repo root." -ForegroundColor Red
        break
    }

    # Find jars
    $jarList = Find-Jars -dirs $jarDirs
    if ($jarList.Count -gt 0) {
        Write-Host " Found JARs:" -ForegroundColor DarkGray
        $jarList | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }
    } else {
        Write-Host " No JAR files found in common locations. Proceeding without external jars." -ForegroundColor Yellow
    }

    # Compile
    Write-Host " Compiling..." -ForegroundColor Yellow
    $javacArgs = @("-encoding", "UTF-8", "-d", $bin)
    if ($jarList.Count -gt 0) { $javacArgs += @("-cp", ($jarList -join $sep)) }
    $javacArgs += $javaFiles

    & javac @javacArgs
    $compileExit = $LASTEXITCODE
    if ($compileExit -ne 0) {
        Write-Host "❌ Compilation failed (exit code $compileExit). Fix errors and the script will retry." -ForegroundColor Red
        Start-Sleep -Seconds 5
        continue
    }

    # Copy resources into bin so getResource('/resources/...') works
    $srcResources = Join-Path $src "resources"
    if (Test-Path $srcResources) {
        Write-Host " Copying resources to $bin..." -ForegroundColor DarkGray
        Copy-Item -Path $srcResources -Destination $bin -Recurse -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host " Warning: resources folder not found at '$srcResources'." -ForegroundColor Yellow
    }

    # Resolve absolute paths for runtime classpath
    $binFull = (Resolve-Path $bin).Path
    $jarFullList = @()
    foreach ($j in $jarList) {
        try { $jarFullList += (Resolve-Path $j).Path } catch { $jarFullList += $j }
    }

    # Show a quick check for the main class file
    $mainClassFile = Join-Path $binFull "integration\MainFrame.class"
    Write-Host " Checking for main class file: $mainClassFile" -ForegroundColor DarkGray
    if (-not (Test-Path $mainClassFile)) {
        Write-Host " Warning: MainFrame.class not found at expected location." -ForegroundColor Yellow
        Write-Host " Listing classes under $binFull (first 200 entries):" -ForegroundColor DarkGray
        Get-ChildItem -Recurse -Path $binFull -File -ErrorAction SilentlyContinue | Select-Object -First 200 | ForEach-Object { Write-Host "  $_.FullName" -ForegroundColor DarkGray }
    }

    Write-Host "✅ Compilation succeeded!" -ForegroundColor Green
    Write-Host "🚀 Running application..." -ForegroundColor Cyan

    $runCpParts = @($binFull) + $jarFullList
    $runCp = $runCpParts -join $sep

    Write-Host " Runtime classpath:" -ForegroundColor DarkGray
    Write-Host "  $runCp" -ForegroundColor DarkGray
    Write-Host " Command: java -cp `"$runCp`" integration.MainFrame" -ForegroundColor DarkGray

    # Launch Java
    & java -cp $runCp integration.MainFrame
    $runExit = $LASTEXITCODE
    Write-Host "Application exited with code $runExit" -ForegroundColor DarkGray

    Write-Host "`n⏳ Restarting in 5 seconds... (Ctrl+C to stop)" -ForegroundColor DarkGray
    Start-Sleep -Seconds 5
}