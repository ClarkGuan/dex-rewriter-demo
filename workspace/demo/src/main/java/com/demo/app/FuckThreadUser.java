package com.demo.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FuckThreadUser {
    public static void testNewThread() {
        new Thread() {
            @Override
            public void run() {
                System.out.println("测试 new Thread() 情况");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void testExecutorService() {
        new Thread() {
            @Override
            public void run() {
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("测试线程池创建情况");
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                executorService.submit(runnable);
                executorService.submit(runnable);
                executorService.submit(runnable);
                executorService.submit(runnable);
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown();
                }
            }
        }.start();
    }
}
