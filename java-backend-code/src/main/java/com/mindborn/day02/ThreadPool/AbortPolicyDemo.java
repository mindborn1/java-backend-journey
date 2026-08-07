package com.mindborn.day02.ThreadPool;

import java.util.concurrent.*;

/**
 * 【拒绝策略1】AbortPolicy（中止策略）
 * 这是线程池的默认拒绝策略。
 * 当线程池无法接受新任务时（核心线程数满了 + 队列满了 + 最大线程数也满了），
 * 直接抛出 RejectedExecutionException 异常，调用者需要自己处理这个异常。
 * 适用场景：希望快速失败，让调用方感知到系统负载过高。
 */
public class AbortPolicyDemo {

    public static void main(String[] args) {

        // ========================================
        // 1. 创建线程池（故意设小容量，快速触发拒绝）
        // ========================================
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,                          // 核心线程数：常驻1个线程
                2,                          // 最大线程数：最多只能有2个线程（1核心+1非核心）
                60L,                        // 非核心线程空闲60秒后被回收
                TimeUnit.SECONDS,           // 时间单位：秒
                new LinkedBlockingQueue<>(1),  // 任务队列：容量只有1个！这是为了演示拒绝故意设小的
                Executors.defaultThreadFactory(), // 线程工厂：负责给线程起名字
                new ThreadPoolExecutor.AbortPolicy() // 【重点】拒绝策略：直接抛异常
        );

        // ========================================
        // 2. 提交4个任务，第4个会触发拒绝
        // ========================================
        // 为什么4个会拒绝？线程池的承载能力 = 最大线程数 + 队列容量 = 2 + 1 = 3
        // 所以第4个任务（索引3）一定会被拒绝
        for (int i = 0; i < 4; i++) {

            // Lambda里不能直接用循环变量i，必须复制一份final的
            final int taskId = i;

            try {
                // execute() 是异步提交：把任务丢给线程池就立即返回，不会等任务执行完
                executor.execute(() -> {

                    // 这行代码是在线程池里的某个线程中执行的
                    System.out.println("任务 " + taskId + " 开始执行，线程名：" + Thread.currentThread().getName());

                    try {
                        // 让任务睡5秒，故意占用线程，不让它太快释放
                        // 这样才能让后面的任务进不来，触发拒绝
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // 线程被中断时，重新设置中断标志（这是标准写法）
                        Thread.currentThread().interrupt();
                    }

                    System.out.println("任务 " + taskId + " 执行完毕");
                });

                // 这行在 execute() 外面，表示"任务已经成功提交给线程池"
                // 注意：不是"任务已经执行完"！execute是异步的
                System.out.println("✅ 任务 " + taskId + " 提交成功");

            } catch (RejectedExecutionException e) {
                // 【AbortPolicy 的核心表现】
                // 当线程池满了，execute() 会抛出这个异常
                // 我们在catch里捕获它，程序就不会崩溃
                System.out.println("❌ 任务 " + taskId + " 被拒绝！AbortPolicy 抛出了异常");
            }
        }

        // ========================================
        // 3. 关闭线程池（优雅关闭）
        // ========================================
        // shutdown() 表示：不再接受新任务，但已经提交的任务（包括队列里的）会继续执行完
        executor.shutdown();
    }
}