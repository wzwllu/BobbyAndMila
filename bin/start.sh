#!/bin/bash
# 静默启动 pmp-1.0.0.jar
# 用法: ./start.sh [--debug]

APP_NAME="pmp-1.0.0"
JAR_FILE="../target/${APP_NAME}.jar"
LOG_DIR="../logs"
PID_FILE="../logs/${APP_NAME}.pid"
JVM_OPTS="-Xms256m -Xmx512m"

# 确保日志目录存在
mkdir -p "$LOG_DIR"

# 检查是否已在运行
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "[WARN] ${APP_NAME} 已在运行 (PID: $OLD_PID)"
        exit 1
    fi
    rm -f "$PID_FILE"
fi

# 检查 JAR 文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "[ERROR] JAR 文件不存在: $JAR_FILE"
    exit 1
fi

# 静默启动（后台运行，输出到日志文件）
nohup java $JVM_OPTS \
    -jar "$JAR_FILE" \
    > "${LOG_DIR}/output.log" 2>&1 &

PID=$!
echo $PID > "$PID_FILE"

# 等待几秒确认启动状态
sleep 5
if kill -0 "$PID" 2>/dev/null; then
    echo "[OK] ${APP_NAME} 已启动 (PID: $PID)"
    echo "[INFO] 日志: ${LOG_DIR}/output.log"
    echo "[INFO] 端口: 9082"
else
    echo "[ERROR] ${APP_NAME} 启动失败，请检查日志: ${LOG_DIR}/output.log"
    rm -f "$PID_FILE"
    exit 1
fi