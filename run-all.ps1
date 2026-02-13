# UPIVerse - Start all backend modules and frontend
# Run from repo root: .\run-all.ps1
# Ensure ports 8080-8084 and 3000 are free before running.

$ErrorActionPreference = "Continue"
$root = $PSScriptRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  UPIVerse - Starting all modules" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. UPISim (8081) - start first as central hub
Write-Host "`n[1/6] Starting UPISim (port 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\UPISim'; .\mvnw.cmd spring-boot:run -DskipTests" -WindowStyle Normal

Start-Sleep -Seconds 15

# 2. PayerPSP (8080)
Write-Host "[2/6] Starting PayerPSP (port 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\PayerPSP'; .\mvnw.cmd spring-boot:run -DskipTests" -WindowStyle Normal

Start-Sleep -Seconds 5

# 3. PayeePSP (8082)
Write-Host "[3/6] Starting PayeePSP (port 8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\PayeePSP'; .\mvnw.cmd spring-boot:run -DskipTests" -WindowStyle Normal

# 4. RemitterBank (8083)
Write-Host "[4/6] Starting RemitterBank (port 8083)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\RemitterBank'; .\mvnw.cmd spring-boot:run -DskipTests" -WindowStyle Normal

# 5. BeneficiaryBank (8084)
Write-Host "[5/6] Starting BeneficiaryBank (port 8084)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\BeneficiaryBank'; .\mvnw.cmd spring-boot:run -DskipTests" -WindowStyle Normal

Start-Sleep -Seconds 5

# 6. Frontend (3000)
Write-Host "[6/6] Starting Frontend (port 3000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\frontend\frontend'; npm start" -WindowStyle Normal

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  All modules launched in new windows" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "  UPISim:        http://localhost:8081" -ForegroundColor White
Write-Host "  PayerPSP:      http://localhost:8080" -ForegroundColor White
Write-Host "  PayeePSP:      http://localhost:8082" -ForegroundColor White
Write-Host "  RemitterBank:  http://localhost:8083" -ForegroundColor White
Write-Host "  BeneficiaryBank: http://localhost:8084" -ForegroundColor White
Write-Host "  Frontend:      http://localhost:3001" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
