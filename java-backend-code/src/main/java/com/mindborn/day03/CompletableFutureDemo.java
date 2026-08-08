package com.mindborn.day03;

import java.util.concurrent.CompletableFuture;

/**
 * Day 03 - 知识点4：CompletableFuture 入门
 *
 * 对比 Future：
 *   Future.get() 会阻塞，没法链式操作
 *   CompletableFuture 支持链式调用，异步任务完成后自动触发下一步
 *
 * 核心方法：
 *   supplyAsync()  — 异步执行有返回值的任务
 *   thenApply()    — 拿到结果后继续处理（有返回值）
 *   thenAccept()   — 拿到结果后消费（无返回值）
 *   join()         — 阻塞等待结果（类似 Future.get()，但不抛 checked 异常）
 */
public class CompletableFutureDemo {

    public static void main(String[] args) {
        System.out.println("=== CompletableFuture 链式调用 ===");

        // ★ supplyAsync：异步执行一个有返回值的任务 ★
        // 默认用 ForkJoinPool.commonPool() 线程池（不用自己建）
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("  [线程 " + Thread.currentThread().getName() + "] 查询用户信息...");
            // 模拟耗时操作
            sleep(1000);
            return "张三"; // 异步返回结果
        });

        // ★ thenApply：拿到上一步结果后继续处理 ★
        // 相当于 Stream 的 map()，对结果做转换
        CompletableFuture<String> greetingFuture = future.thenApply(name -> {
            System.out.println("  [线程 " + Thread.currentThread().getName() + "] 拼接问候语...");
            return "你好，" + name + "！欢迎回来";
        });

        // ★ thenAccept：消费最终结果（无返回值）★
        // 相当于 Stream 的 forEach()
        greetingFuture.thenAccept(message -> {
            System.out.println("  [线程 " + Thread.currentThread().getName() + "] " + message);
        });

        // 主线程等一会儿，让异步任务跑完
        System.out.println("  主线程等待异步任务完成...");
        sleep(3000);
        System.out.println("  完成！");
        // 主线程等一会儿，让异步任务跑完
        System.out.println("  主线程等待异步任务完成...");
        sleep(3000);
        System.out.println("  完成！");

        // ← 在这里加上下面这段 ↓

        // ============ 异常处理：exceptionally ============
        System.out.println("\n=== exceptionally 异常兜底 ===");

        CompletableFuture<String> errorFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("  [线程 " + Thread.currentThread().getName() + "] 模拟查询失败...");
            throw new RuntimeException("数据库连接超时");
        });

        errorFuture.exceptionally(ex -> {
            System.out.println("  ⚠️ 捕获到异常: " + ex.getMessage());
            return "默认用户（兜底）";
        }).thenAccept(result -> {
            System.out.println("  最终结果: " + result);
        });

        sleep(2000);

    }

    // 小工具方法：让线程睡一会儿
    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}