package com.mindborn.day03;

import java.util.concurrent.*;

/**
 * Day 03 - 知识点3：submit() 的异常处理优势
 *
 * execute() 里的异常 → 直接炸，没法在外部捕获
 * submit() 里的异常  → 被 Future 兜住，get() 时才暴露，可以 try-catch 处理
 */

public class FutureExceptionDemo {

    public static void main(String[] args) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2,
                4,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                new ThreadPoolExecutor.AbortPolicy()
        );

        //========对比 ：execute 的异常========
        System.out.println("===execute的异常===");

        try {
            pool.execute(() -> {
                //这个异常直接炸在线程池的线程里，外面的try -catch抓不到！
                throw new RuntimeException("execute里的异常");
            });
            System.out.println("execute 提交成功（但是其实里面的异常已经炸了）");
        } catch (Exception e) {
            //这行永远不会执行！因为execute的异常不往外抛
            System.out.println("抓到execute的异常：" + e.getMessage());
        }

        //等一下让execute的异常打印出来
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        // ==========对比submit的异常==========
        System.out.println("\n===submit的异常===");

        Future<String> future = pool.submit(() ->{
            //这个异常不会立刻炸，而是被塞进Future里
            throw new RuntimeException("Callable里的异常");
        });

        try {
            //调用get()的时候，异常才会以ExecutionException形式抛出
            String result = future.get();
            System.out.println("结果：" + result);//这行不会执行
        } catch (ExecutionException e) {
            //getCause()能拿到原始异常
            System.out.println("√ 捕获到原始异常：" + e.getCause().getMessage());
            System.out.println("   advantage:submit的异常可追踪、可恢复！");
        }catch (Exception e) {
            e.printStackTrace();
        };

        pool.shutdown();
    }
}
