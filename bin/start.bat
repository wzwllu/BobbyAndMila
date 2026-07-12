@echo off
chcp 65001 >nul
REM 静默启动 pmp-1.0.0.jar (Windows)
REM 用法: start.bat

set APP_NAME=pmp-1.0.0
set JAR_FILE=..\target\%APP_NAME%.jar
set LOG_DIR=..\logs
set PID_FILE=%LOG_DIR%\%APP_NAME%.pid

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM 检查是否已在运行
if exist "%PID_FILE%" (
    set /p OLD_PID=<"%PID_FILE%"
    tasklist /FI "PID eq %OLD_PID%" 2>nul | findstr "%OLD_PID%" >nul
    if not errorlevel 1 (
        echo [WARN] %APP_NAME% 已在运行 (PID: !OLD_PID!)
        exit /b 1
    )
    del "%PID_FILE%"
)

REM 检查 JAR 文件
if not exist "%JAR_FILE%" (
    echo [ERROR] JAR 文件不存在: %JAR_FILE%
    exit /b 1
)

REM 后台启动，输出重定向到日志文件
start "pmp" /B java -Xms256m -Xmx512m -jar "%JAR_FILE%" > "%LOG_DIR%\output.log" 2>&1

REM 获取 PID 并保存
set PID=!ERRORLEVEL!
echo !PID! > "%PID_FILE%"

echo [OK] %APP_NAME% 已启动
echo [INFO] 日志: %LOG_DIR%\output.log
echo [INFO] 端口: 9082