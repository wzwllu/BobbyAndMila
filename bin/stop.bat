@echo off
chcp 65001 >nul
REM 静默停止 pmp-1.0.0.jar (Windows)
REM 用法: stop.bat

set APP_NAME=pmp-1.0.0
set PID_FILE=..\logs\%APP_NAME%.pid

REM 方式一：通过 PID 文件停止
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /FI "PID eq %PID%" 2>nul | findstr "%PID%" >nul
    if not errorlevel 1 (
        echo [INFO] 正在停止 %APP_NAME% (PID: !PID!) ...
        taskkill /PID !PID! /F >nul 2>&1
        if not errorlevel 1 (
            echo [OK] %APP_NAME% 已停止
        ) else (
            echo [ERROR] 停止失败
        )
    ) else (
        echo [WARN] 进程 (PID: !PID!) 不存在，清理 PID 文件
    )
    del "%PID_FILE%" 2>nul
    exit /b 0
)

REM 方式二：通过进程名查找停止
for /f "tokens=2 delims=," %%a in ('wmic process where "commandline like '%%%APP_NAME%%.jar%%'" get processid /format:csv 2^>nul') do (
    set PID=%%a
)
if defined PID (
    echo [INFO] 正在停止 %APP_NAME% (PID: !PID!) ...
    taskkill /PID !PID! /F >nul 2>&1
    echo [OK] %APP_NAME% 已停止
) else (
    echo [INFO] %APP_NAME% 未在运行
)