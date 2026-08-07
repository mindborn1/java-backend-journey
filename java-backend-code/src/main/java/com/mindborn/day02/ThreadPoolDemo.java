package com.mindborn.day02;

import java.util.concurrent.*;

public class ThreadPoolDemo {
    public static void main(String[] args) {

        //1.创建线程池（面试重点!!7个参数）
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,  //核心线程数：常驻线程数量
                10,  //最大线程数：最多能开多少个线程
                60L,  //空闲线程存活时间
                TimeUnit.SECONDS,  //时间单位
                new LinkedBlockingQueue<>(100),  //任务队列：最多排一百个任务
                        Executors.defaultThreadFactory()  //拒绝策略：队列满了怎么办
        );

        //2.提交任务
        for (int i = 0; i < 15; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("任务" + taskId + "正在执行，线程名：" +Thread.currentThread().getName());
                try{
                    Thread.sleep(1000);//模拟任务执行一秒
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                System.out.println("任务" + taskId + "执行完毕");
            });
        }
        //3. 关闭线程池
        executor.shutdown();
    }
}
