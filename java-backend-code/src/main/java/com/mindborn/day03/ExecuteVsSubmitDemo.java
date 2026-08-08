
package com.mindborn.day03;

import java.util.concurrent.*;

/**
 * Day03 - 知识点一：execute() vs submit()
 *
 * 核心区别：
 * 1 execute() -只接受runnable,没有返回值，异常直接炸
 * 2 submit() - 可以接受callable,返回Future(欠条)，异常被兜住
 */

public class ExecuteVsSubmitDemo {

    public static void main(String[] args){
        //day02 手动创建线程池（七个参数
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2,     //核心线程数
                4,     //最大线程数
                60,TimeUnit.SECONDS,    //空闲线程存活时间
                new LinkedBlockingQueue<>(10),    //任务队列容量10
                new ThreadPoolExecutor.AbortPolicy()     //拒绝策略：满了就抛异常
        );

        //==============对比1:有没有返回值================
        System.out.println("=== 对比1：execute vs submit 返回值 ===");

        //execute  -丢进去就完事，啥都拿不回来
        pool.execute(() ->{
            System.out.println("execute:我去干活了，但是你拿不到任何结果");
        });

        //submit +Callable -能拿到返回值
        Future<String> future = pool.submit(() ->{
            Thread.sleep(1000);  //模拟耗时操作 （比如查数据库）
            return "submit:这是我干完的结果！";    //Callable 可以返回值
        });

        //主线程不用傻等，可以先去干别的事
        System.out.println("主线程拿到了Future（欠条），先去干别的事...");

        try{
            //get() -兑现欠条，如果任务没跑完就阻塞等待
            String result = future.get();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //优雅关闭线程池
        pool.shutdown();
    }
}
