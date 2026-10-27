$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$verification = Get-Content -LiteralPath (Join-Path $projectRoot 'reports/tag-package.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not $verification.passed) { throw 'Package verification has not passed.' }
$sourceJar = Join-Path $projectRoot 'build/libs/TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar'
$sourceHash = (Get-FileHash -LiteralPath $sourceJar -Algorithm SHA256).Hash
if ($sourceHash -ne $verification.sha256) { throw 'The build changed after package verification.' }

# Use Unicode escapes so this script also works in Windows PowerShell 5.1 without a BOM.
$serverName = -join (0x7EBF, 0x6846, 0x7684, 0x670D, 0x52A1, 0x5668 | ForEach-Object { [char]$_ })
$mods = Join-Path ('D:\' + $serverName) 'versions/TFC_Wild_Fire/mods'
if (-not (Test-Path -LiteralPath $mods -PathType Container)) { throw 'Target mods directory is missing.' }
$fileName = Split-Path -Leaf $sourceJar
$otherBuilds = @(Get-ChildItem -LiteralPath $mods -Filter '*florae*.jar' -File | Where-Object { $_.Name -ne $fileName })
if ($otherBuilds.Count -gt 0) { throw 'Other Florae jars exist; refusing to install a duplicate.' }
$destination = Join-Path $mods $fileName
$backup = $null
$previousHash = $null
if (Test-Path -LiteralPath $destination) {
    $previousHash = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash
    $backup = $destination + '.bak-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
    Copy-Item -LiteralPath $destination -Destination $backup
    if ((Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash -ne $previousHash) {
        throw 'Backup verification failed.'
    }
}

# Copy first, verify, and only then replace the installed jar. Retain all backups.
$staged = $destination + '.codex-staged'
Copy-Item -LiteralPath $sourceJar -Destination $staged
if ((Get-FileHash -LiteralPath $staged -Algorithm SHA256).Hash -ne $sourceHash) {
    throw 'Staged copy verification failed.'
}
Copy-Item -LiteralPath $staged -Destination $destination -Force
$installedHash = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash
$matched = $sourceHash -eq $installedHash
$result = [ordered]@{ source = $sourceJar; destination = $destination; backup = $backup;
    previous_sha256 = $previousHash; sha256 = $sourceHash; installed_sha256 = $installedHash; match = $matched }
$result | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $projectRoot 'reports/deployment.json') -Encoding UTF8
$result | ConvertTo-Json
if (-not $matched) { throw 'Installed SHA256 does not match.' }
