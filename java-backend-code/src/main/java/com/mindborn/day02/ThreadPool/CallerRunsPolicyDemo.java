package com.mindborn.day02.ThreadPool;

import java.util.concurrent.*;

/**
 * 【拒绝策略2】CallerRunsPolicy（调用者运行策略）
 *
 * 当线程池满了（核心线程数满了 + 队列满了 + 最大线程数也满了），
 * 不会抛异常，而是让"提交任务的线程"（也就是调用 execute() 的那个线程，通常是 main 线程）
 * 自己来执行这个任务。
 *
 * 适用场景：希望任务不被丢弃，同时通过降低提交速度来做"流量削峰"（因为调用者线程自己去执行任务，会被阻塞）。
 */
public class CallerRunsPolicyDemo {

    public static void main(String[] args) {

        // ========================================
        // 1. 创建线程池（参数和AbortPolicyDemo一样，为了对比效果）
        // ========================================
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,                          // 核心线程数：常驻1个线程
                2,                          // 最大线程数：最多2个线程
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1),  // 队列容量：只有1个
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 【重点】拒绝策略：调用者自己执行
        );

        // ========================================
        // 2. 提交4个任务，第4个会触发拒绝策略
        // ========================================
        for (int i = 0; i < 4; i++) {
            final int taskId = i;

            // 【注意】这里没有 try-catch 了！
            // 因为 CallerRunsPolicy 不会抛异常，所以不需要捕获
            executor.execute(() -> {

                // 打印当前执行任务的线程名，这是观察 CallerRunsPolicy 的关键！
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