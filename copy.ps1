$source = "run\plugins\BetterModel\build.zip"
$destination = "..\"
if (Test-Path $source) {
    Copy-Item -Path $source -Destination $destination -Force
    Write-Host "--------------------------------------------------" -ForegroundColor Green
    Write-Host "Successfully copied build.zip to resource pack folder." -ForegroundColor Green
    Write-Host "--------------------------------------------------" -ForegroundColor Green
} else {
    Write-Host "Source file not found: $source" -ForegroundColor Red
}
