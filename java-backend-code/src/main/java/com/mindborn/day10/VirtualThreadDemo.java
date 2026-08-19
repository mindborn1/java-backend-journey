package com.mindborn.day10;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Virtual Threads 虚拟线程 Demo
 * 对比：平台线程 vs 虚拟线程 创建 1000 个线程的性能
 */
public class VirtualThreadDemo {
    public static void main(String[] args) throws Exception {
        // 任务：每个线程sleep 100ms，模拟I/O等待
        Runnable task = () -> {
            try {
                Thread.sleep(100);// 虚拟线程在这里会被 Unmount，不占用 OS 线程
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        int count = 1000;

        // ========== 1. 平台线程（传统线程池）==========
        System.out.println("=== 平台线程 ===");
        Instant start1 = Instant.now();

        // 线程池大小设为 100，1000 个任务要排队
        try (var executor = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < count; i++) {
                executor.submit(task);
            }
        }//等所有任务完成，executor关闭

        Duration d1 = Duration.between(start1, Instant.now());
        System.out.println("平台线程(100池) 耗时: " + d1.toMillis() + "ms");
        // 1000 个任务，100 个线程，每个 sleep 100ms
        // 理论最少: 10 批 * 100ms = 1000ms

        // ========== 2. 虚拟线程 ==========
        System.out.println("\n=== 虚拟线程 ===");
        Instant start2 = Instant.now();

        // 每个任务一个虚拟线程，1000 个虚拟线程同时跑
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executor.submit(task);
            }
        }

        Duration d2 = Duration.between(start2, Instant.now());
        System.out.println("虚拟线程 耗时: " + d2.toMillis() + "ms");
        // 1000 个虚拟线程，遇到 sleep 就卸载，几乎同时完成
        // 理论: ~100ms + 调度开销
    }

}
