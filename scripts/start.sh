#!/bin/bash
# ============================================
# ELK 日志平台一键启动脚本
# 适用: macOS / Linux
# ============================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "================================================"
echo " ELK 日志平台 启动中..."
echo "================================================"

cd "$PROJECT_DIR"

# 1. 编译 sample-app
echo ""
echo "[1/3] 编译 Spring Boot 示例应用..."
if command -v mvn &> /dev/null; then
    cd sample-app && mvn clean package -DskipTests -q && cd ..
else
    echo "未检测到 Maven，跳过编译。请手动编译 sample-app 后重新运行。"
    echo "  cd sample-app && mvn clean package -DskipTests"
    exit 1
fi

# 2. 构建 Docker 镜像
echo ""
echo "[2/3] 构建 Docker 镜像..."
docker build -t sample-app:1.0.0 sample-app/

# 3. 启动 ELK 全家桶
echo ""
echo "[3/3] 启动 ELK + Filebeat + Sample App..."
docker-compose up -d

echo ""
echo "================================================"
echo " 启动完成！"
echo "================================================"
echo ""
echo "访问地址:"
echo "  Kibana:      http://localhost:5601"
echo "  Sample App:  http://localhost:8080"
echo "  ES API:      http://localhost:9200"
echo ""
echo "测试接口:"
echo "  curl http://localhost:8080/api/user/1"
echo "  curl http://localhost:8080/api/trigger-error?type=2"
echo "  curl -X POST http://localhost:8080/api/order -H 'Content-Type: application/json' -d '{\"product\":\"MacBook\",\"amount\":\"12999\"}'"
echo "  curl -X POST http://localhost:8080/api/batch-process?count=20"
echo ""
echo "查看日志: docker logs sample-app -f"
echo "停止服务: bash scripts/stop.sh"