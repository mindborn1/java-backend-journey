package com.mindborn.day02.ThreadPool;

import java.util.concurrent.*;

/**
 * 【拒绝策略3】DiscardPolicy（静默丢弃策略）
 *
 * 当线程池满了（核心线程数满了 + 队列满了 + 最大线程数也满了），
 * 直接把这个任务"悄悄扔掉"，不抛异常，也不执行，调用方完全感知不到。
 *
 * ⚠️ 这是一个"危险"的策略！任务丢了都不知道，像石沉大海。
 *
 * 适用场景：不重要、可容忍丢失的任务，比如日志记录、埋点统计、非关键定时任务。
 *         核心业务千万别用这个！
 */
public class DiscardPolicyDemo {

    public static void main(String[] args) {

        // ========================================
        // 1. 创建线程池
        // ========================================
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,                          // 核心线程数：1个
                2,                          // 最大线程数：2个
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1),  // 队列容量：1个
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy() // 【重点】拒绝策略：静默丢弃
        );

        // ========================================
        // 2. 提交4个任务
        // ========================================
        for (int i = 0; i < 4; i++) {
            final int taskId = i;

            // 和 CallerRunsPolicy 一样，不需要 try-catch，因为不抛异常
            executor.execute(() -> {
                System.out.println("任务 " + taskId + " 开始执行，线程名：" + Thread.currentThread().getName());

                try {
                    Thread.sleep(5000); // 睡5秒，拖住线程
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println("任务 " + taskId + " 执行完毕");
            });

            System.out.println("✅ 任务 " + taskId + " 提交成功");
        }

        // ========================================
        // 3. 关闭线程池
        // ========================================
        executor.shutdown();
    }
}