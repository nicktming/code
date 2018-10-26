package com.futuretask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureTaskTest {
    private final FutureTask<ProductInfo> future = new FutureTask<ProductInfo>(
            new Callable<ProductInfo>() {
                public ProductInfo call() throws InterruptedException {
                    return loadProductInfo();
                }
            });

    public ProductInfo loadProductInfo() throws InterruptedException {
        System.err.println(Thread.currentThread().getName() + "=====waiting========");
        Thread.sleep(5000);
        return new ProductInfo();
    }

    private final Thread thread = new Thread(future);

    public void start() {
        thread.start();
    }

    public ProductInfo get() throws InterruptedException, ExecutionException,
            TimeoutException {
        try{
            return future.get(10, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            //future.cancel(true); //取消任务
            System.err.println("火速返回，失败！");
            return future.get(10, TimeUnit.SECONDS);
        }
    }

    /**
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws InterruptedException,
            ExecutionException, TimeoutException {
        FutureTaskTest test = new FutureTaskTest();
        test.start();
        System.err.println("=======begin========");
        System.err.println(test.get());
        System.err.println("=======end========");
    }

    static class ProductInfo {
        public String toString() {
            return "**ProductInfo**";
        }
    }
}
