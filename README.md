# ELK 日志平台 —— 跨平台统一日志管理

## 项目背景

Windows 环境下查日志需要频繁远程登录服务器、拷贝文件、用记事本/Notepad++ 搜索，效率极低。macOS 和 Linux 有 `grep`、`tail -f`、`less` 等原生工具链，而 Windows 用户只能靠 GUI 工具逐个翻文件。

本项目基于 ELK（Elasticsearch + Logstash + Kibana）搭建一套**跨平台统一日志管理平台**，只需浏览器访问 Kibana 即可完成日志搜索、过滤、可视化，彻底消除操作系统差异带来的效率鸿沟。

## 架构

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Sample App  │────▶│   Filebeat   │────▶│   Logstash   │────▶│ Elasticsearch │
│  (JSON Logs) │     │ (采集+转发)  │     │ (解析+清洗)  │     │  (存储+索引)  │
└──────────────┘     └──────────────┘     └──────────────┘     └──────┬───────┘
                                                                      │
                                                                      ▼
                                                               ┌──────────────┐
                                                               │    Kibana    │
                                                               │ (搜索+可视化) │
                                                               └──────────────┘
```

## 快速开始

### 前提条件

- Docker & Docker Compose
- Maven 3.6+
- JDK 8+

### 一键启动

```bash
bash scripts/start.sh
```

### 手动启动

```bash
# 1. 编译示例应用
cd sample-app && mvn clean package -DskipTests && cd ..

# 2. 构建镜像
docker build -t sample-app:1.0.0 sample-app/

# 3. 启动全套服务
docker-compose up -d
```

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| Kibana | http://localhost:5601 | 日志搜索、可视化看板 |
| Sample App | http://localhost:8080 | 示例应用 API |
| Elasticsearch | http://localhost:9200 | ES REST API |

## 测试接口

Sample App 内置了 4 个接口，覆盖 INFO / WARN / ERROR 三种日志级别：

```bash
# 正常查询
curl http://localhost:8080/api/user/1

# 创建订单
curl -X POST http://localhost:8080/api/order \
  -H 'Content-Type: application/json' \
  -d '{"product":"MacBook Pro","amount":"12999"}'

# 触发错误日志
curl "http://localhost:8080/api/trigger-error?type=1"   # WARN
curl "http://localhost:8080/api/trigger-error?type=2"   # ERROR + 异常堆栈
curl "http://localhost:8080/api/trigger-error?type=3"   # INFO + WARN 混合

# 批量处理（高频率日志）
curl -X POST "http://localhost:8080/api/batch-process?count=20"
```

## Kibana 使用指南

### 1. 创建 Index Pattern

首次打开 Kibana → Stack Management → Index Patterns → Create index pattern：

- Index pattern name: `app-logs-*`
- Time field: `@timestamp`

### 2. 搜索日志

进入 Discover，可以按以下条件搜索：

```
# 按日志级别过滤
log_level: ERROR

# 按请求路径过滤
request_uri: "/api/order"

# 按 traceId 追踪完整链路
traceId: "a3f2c8b1"

# 组合查询
log_level: ERROR AND app_name: "sample-app"

# 慢请求（响应时间 > 200ms）
response_time_ms: >200
```

### 3. 创建可视化看板

建议创建的图表：
- 日志级别分布（饼图）
- 请求量时间趋势（折线图）
- 接口响应时间 P50/P99（指标）
- Top N 错误类型（柱状图）
- 错误率趋势（百分比）

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Elasticsearch | 8.12.0 | 日志存储与全文检索 |
| Logstash | 8.12.0 | 日志解析、过滤、清洗 |
| Kibana | 8.12.0 | 日志搜索、可视化 |
| Filebeat | 8.12.0 | 日志采集与转发 |
| Spring Boot | 2.7.18 | 示例应用 |
| logstash-logback-encoder | 7.4 | JSON 格式日志输出 |

## 关键特性

- **结构化日志**：应用以 JSON 格式输出日志，包含 traceId、uri、duration 等字段，便于精确检索
- **多行异常合并**：Filebeat 自动合并 Java 异常堆栈，不会拆成碎片
- **链路追踪**：每个请求携带 traceId，可在 Kibana 中串联完整调用链
- **Docker 化部署**：全组件容器化，一键启动销毁，环境隔离
- **Window 友好**：最终用户只需浏览器，不依赖操作系统工具链