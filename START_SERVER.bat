@echo off
title Spring Boot Server - Lab 06
echo ====================================================
echo Starting Spring Boot Server on http://localhost:8080
echo Keep this window OPEN while testing in browser!
echo ====================================================
cd /d "C:\Users\erbol\Downloads\spring-lab-01\spring-lab-01"
java -jar "target\spring-lab-01-0.0.1-SNAPSHOT.jar"
if %errorlevel% neq 0 (
    echo.
    echo Running via maven wrapper...
    call mvnw.cmd spring-boot:run
)
pause