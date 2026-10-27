# Compile helper: runs a gradle task offline and prints java compiler errors.
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File buildcheck.ps1 [task]
# Default task: compileJava. The log is written to build_curr.log (GBK).
# Paths are derived from this script's location so the file stays ASCII-only.
param(
    [string]$Task = "compileJava"
)

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$workspace = Split-Path -Parent $root
$log = Join-Path $root "build_curr.log"

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:GRADLE_USER_HOME = Join-Path $workspace "gradle-home"

Push-Location $root
try {
    cmd /c "gradlew.bat --no-daemon --offline $Task > build_curr.log 2>&1"
    $code = $LASTEXITCODE
} finally {
    Pop-Location
}

$gbk = [System.Text.Encoding]::GetEncoding(936)
$lines = [System.IO.File]::ReadAllLines($log, $gbk)

$errs = @()
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match [char]0x9519 + [char]0x8BEF + ':') {
        $head = $lines[$i] -replace '^.*?src\\main\\java\\tfcflorae\\', ''
        $detail = ''
        if ($i + 1 -lt $lines.Count -and $lines[$i + 1] -match '^\s{4,}') {
            $detail = $lines[$i + 1].Trim()
        }
        $errs += ($head + "  ||  " + $detail)
    }
}

Write-Host ("===== EXIT=" + $code + "  ERRORS=" + $errs.Count + " =====")
foreach ($e in $errs) { Write-Host $e }
if ($code -ne 0 -and $errs.Count -eq 0) {
    Write-Host "----- no java errors; log tail -----"
    $lines | Select-Object -Last 30
}
