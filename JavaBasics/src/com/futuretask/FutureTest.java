package com.futuretask;


import java.util.HashMap;
import java.util.concurrent.*;

public class FutureTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("-----main thread start------");
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        System.out.println("=====submit asyn task.");

        Future<HashMap<String, String>> future = threadPool.submit(new Callable<HashMap<String, String>>() {
            @Override
            public HashMap<String, String> call() throws Exception {
                return null;
            }
        });

        System.out.println("---submit asyn and return main thread");

        Thread.sleep(1000);
        System.out.println("----------");

        boolean flag = true;

        while (flag) {
            if (future.isDone() && !future.isCancelled()) {
                HashMap<String, String> futureResult = future.get();
                System.out.println("====异步任务返回的结果是："+futureResult.get("futureKey"));
                flag = false;
            }
        }

        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }
}
