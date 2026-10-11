@echo off
cd /d "%~dp0backend"
echo Starting Skill Wallet backend...
mvn spring-boot:run
pause
