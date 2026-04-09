while ($true) {
    Clear-Host
    Write-Host " Rebuilding project..." -ForegroundColor Cyan

    $src = "gect-connect"
    $bin = "$src\bin"

    # ✅ Libraries
    $libs = "mysql-connector-j-9.6.0.jar;flatlaf-3.7.jar;flatlaf-extras-3.7.jar;jsvg-1.7.0.jar"
    $cp = "$bin;$libs"

    # Kill previous Java process
    Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

    # Clean bin
    if (Test-Path $bin) {
        Remove-Item -Recurse -Force $bin -ErrorAction SilentlyContinue
    }
    New-Item -ItemType Directory $bin | Out-Null

    # Get all Java files
    $files = Get-ChildItem -Recurse -Path $src -Filter *.java | Select-Object -ExpandProperty FullName

    Write-Host " Compiling..." -ForegroundColor Yellow

    # ✅ FIX: Use CMD for proper classpath handling
    cmd /c "javac -d $bin -cp `"$libs`" $files"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Compilation failed. Fix errors." -ForegroundColor Red
        Start-Sleep -Seconds 5
        continue
    }

    # Copy resources
    if (Test-Path "$src\resources") {
        Copy-Item -Recurse -Force "$src\resources" "$bin"
    }

    Write-Host "✅ Compilation succeeded!" -ForegroundColor Green
    Write-Host "🚀 Running application..." -ForegroundColor Cyan

    # ✅ FIX: Run using CMD (CRITICAL)
    cmd /c "java -cp `"$cp`" integration.MainFrame"

    Write-Host "`n⏳ Restarting in 5 seconds... (Ctrl+C to stop)" -ForegroundColor DarkGray
    Start-Sleep -Seconds 5
}
