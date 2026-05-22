package com.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api")
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);
    private static final List<String> USERS = Arrays.asList("Alice", "Bob", "Charlie");

    /**
     * 模拟正常业务请求 —— 查询用户
     */
    @GetMapping("/user/{id}")
    public Map<String, Object> getUser(@PathVariable String id) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);
        MDC.put("uri", "/api/user/" + id);
        
        long start = System.currentTimeMillis();
        
        log.info("查询用户开始, userId={}", id);
        
        // 模拟业务逻辑
        try { Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200)); } catch (InterruptedException ignored) {}
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", USERS.get(Math.abs(id.hashCode()) % USERS.size()));
        user.put("traceId", traceId);
        
        long duration = System.currentTimeMillis() - start;
        MDC.put("duration", String.valueOf(duration));
        log.info("查询用户完成, userId={}, duration={}ms", id, duration);
        
        MDC.clear();
        return user;
    }

    /**
     * 模拟创建订单 —— 正常 INFO 日志
     */
    @PostMapping("/order")
    public Map<String, Object> createOrder(@RequestBody Map<String, String> body) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);
        MDC.put("uri", "/api/order");
        
        long start = System.currentTimeMillis();
        
        String orderId = "ORD-" + System.currentTimeMillis();
        log.info("创建订单开始, orderId={}, product={}", orderId, body.get("product"));
        
        try { Thread.sleep(ThreadLocalRandom.current().nextInt(80, 300)); } catch (InterruptedException ignored) {}
        
        long duration = System.currentTimeMillis() - start;
        MDC.put("duration", String.valueOf(duration));
        log.info("订单创建成功, orderId={}, amount={}", orderId, body.getOrDefault("amount", "0"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "SUCCESS");
        result.put("traceId", traceId);
        
        MDC.clear();
        return result;
    }

    /**
     * 模拟异常场景 —— 触发 WARN / ERROR
     */
    @GetMapping("/trigger-error")
    public Map<String, Object> triggerError(@RequestParam(defaultValue = "1") int type) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);
        MDC.put("uri", "/api/trigger-error");
        
        Map<String, Object> result = new HashMap<>();
        result.put("traceId", traceId);
        
        switch (type) {
            case 1:
                log.warn("业务警告: 库存不足, productId=SKU-{}", ThreadLocalRandom.current().nextInt(1000, 9999));
                result.put("message", "WARN 日志已触发");
                break;
            case 2:
                try {
                    throw new RuntimeException("模拟数据库连接超时");
                } catch (RuntimeException e) {
                    log.error("系统异常: 数据库连接失败, retry=3", e);
                }
                result.put("message", "ERROR 日志已触发（含异常堆栈）");
                break;
            case 3:
                log.info("访问量激增, qps={}", ThreadLocalRandom.current().nextInt(500, 2000));
                log.warn("线程池使用率超过 80%, activeThreads={}", ThreadLocalRandom.current().nextInt(50, 100));
                result.put("message", "INFO + WARN 日志已触发");
                break;
        }
        
        MDC.clear();
        return result;
    }

    /**
     * 模拟批量操作 —— 高频率日志
     */
    @PostMapping("/batch-process")
    public Map<String, Object> batchProcess(@RequestParam(defaultValue = "10") int count) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);
        MDC.put("uri", "/api/batch-process");
        
        long start = System.currentTimeMillis();
        log.info("批量处理开始, totalCount={}", count);
        
        int success = 0, fail = 0;
        for (int i = 0; i < count; i++) {
            try { Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50)); } catch (InterruptedException ignored) {}
            if (ThreadLocalRandom.current().nextDouble() < 0.15) {
                log.warn("第 {} 条处理失败: 数据格式校验不通过", i + 1);
                fail++;
            } else {
                success++;
            }
        }
        
        long duration = System.currentTimeMillis() - start;
        MDC.put("duration", String.valueOf(duration));
        log.info("批量处理完成, total={}, success={}, fail={}, duration={}ms", count, success, fail, duration);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", count);
        result.put("success", success);
        result.put("fail", fail);
        result.put("duration", duration);
        result.put("traceId", traceId);
        
        MDC.clear();
        return result;
    }
}