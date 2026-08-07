package com.mindborn.day02.ThreadPool;

import java.util.concurrent.*;

/**
 * 【拒绝策略4】DiscardOldestPolicy（丢弃最老任务策略）
 *
 * 当线程池满了，它会丢弃队列里"等待时间最长"的那个任务（队头的任务），
 * 然后把新任务放进队列里。
 *
 * 换句话说：为了接纳新任务，它"牺牲"了最老的任务。
 *
 * 适用场景：新任务比旧任务更重要。比如实时消息推送、最新行情数据，
 *         旧数据已经过时了，丢掉不可惜。
 */
public class DiscardOldestPolicyDemo {

    public static void main(String[] args) {

        // ========================================
        // 1. 创建线程池
        // ========================================
        // 这里把最大线程数也设为1，这样更容易触发拒绝，效果更明显
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,                          // 核心线程数：1个
                1,                          // 最大线程数：1个（不创建非核心线程）
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1),  // 队列容量：1个
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy() // 【重点】丢弃最老的任务
        );

        // ========================================
        // 2. 提交3个任务
        // ========================================
        // 承载能力 = 线程1 + 队列1 = 2，第3个任务会触发拒绝
        for (int i = 0; i < 3; i++) {
            final int taskId = i;

            executor.execute(() -> {
                System.out.println("任务 " + taskId + " 开始执行，线程名：" + Thread.currentThread().getName());

                try {
                    Thread.sleep(10000); // 睡10秒，让线程一直被占用
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