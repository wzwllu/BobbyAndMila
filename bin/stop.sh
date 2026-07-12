#!/bin/bash
# 静默停止 pmp-1.0.0.jar
# 用法: ./stop.sh

APP_NAME="pmp-1.0.0"
PID_FILE="../logs/${APP_NAME}.pid"

# 方式一：通过 PID 文件停止
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        echo "[INFO] 正在停止 ${APP_NAME} (PID: $PID) ..."
        kill "$PID"
        # 等待进程退出（最多 30 秒）
        for i in $(seq 1 30); do
            if ! kill -0 "$PID" 2>/dev/null; then
                break
            fi
            sleep 1
        done
        # 强制终止
        if kill -0 "$PID" 2>/dev/null; then
            echo "[WARN] 进程未响应，强制终止..."
            kill -9 "$PID" 2>/dev/null
        fi
        echo "[OK] ${APP_NAME} 已停止"
    else
        echo "[WARN] 进程 (PID: $PID) 不存在，清理 PID 文件"
    fi
    rm -f "$PID_FILE"
    exit 0
fi

# 方式二：通过进程名查找停止
PID=$(ps aux | grep "${APP_NAME}.jar" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "[INFO] 正在停止 ${APP_NAME} (PID: $PID) ..."
    kill "$PID"
    sleep 5
    # 确认是否已停止
    if kill -0 "$PID" 2>/dev/null; then
        echo "[WARN] 进程未响应，强制终止..."
        kill -9 "$PID" 2>/dev/null
    fi
    echo "[OK] ${APP_NAME} 已停止"
else
    echo "[INFO] ${APP_NAME} 未在运行"
fi