package com.mindborn.day03;

import java.util.concurrent.*;

/**
 * Day03 - 知识点2
 */

public class FutureTimeoutDemo {

    public static void main(String[] args) {
        //创建线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2,
                4,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                new ThreadPoolExecutor.AbortPolicy()
        );

        //=========演示1：正常超市场景=========
        System.out.println("===演示1：超时场景===");

        //模拟一个需要三秒的慢任务（比如调外部接口超时）
        Future<String> slowFuture = pool.submit(() -> {
            System.out.println("[线程" + Thread.currentThread().getName() + "[开始执行慢任务...");
            Thread.sleep(3000); //模拟耗时3秒
            return "慢任务的结果";
        });

        try {
            //  只等两秒，超过就抛TimeoutException
            String result = slowFuture.get(2, TimeUnit.SECONDS);
            System.out.println("拿到结果：" + result);
        } catch (TimeoutException e) {
            //超时了！主动取消任务，避免线程浪费
            System.out.println("⚠等了两秒没有返回，超时了！");
            System.out.println(" 取消任务... ");
            slowFuture.cancel(true);//true = 允许中断正在执行的任务
            System.out.println("任务是否被取消：" + slowFuture.isCancelled());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //========演示2：正常返回场景=========
        System.out.println("\n===演示2：正常返回（不超时）");

        Future<String> fastFuture = pool.submit(() -> {
            Thread.sleep(500); //只耗时0.5秒
            return "快速任务的结果";
        });

        try {
            //给两秒超时，实际上0。5秒就返回了，不会超时
            String result = fastFuture.get(2, TimeUnit.SECONDS);
            System.out.println("  拿到结果：" + result);
            System.out.println("  任务是否完成：" + fastFuture.isDone());
        } catch (TimeoutException e) {
            System.out.println("超时了");
        } catch (Exception e) {
            e.printStackTrace();
        }

        pool.shutdown();
        System.out.println("\n线程池已关闭");

    }
}
