package com.sourcecode.concurrencytools_CyclicBarrier;

import java.util.concurrent.TimeUnit;

public class CyclicBarrierTest5 {
    static CyclicBarrier c = new CyclicBarrier(3);
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        for (int i = 0; i < 2; i++) {
            Thread thread = new MyThread();
            thread.start();
        }
        TimeUnit.SECONDS.sleep(3);
        System.out.println(Thread.currentThread().getName() + "------>" + "tries to wait!");
        c.await();
        System.out.println(Thread.currentThread().getName() + "------>" + "finishes!");
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " tries to wait!");
                c.await(1, TimeUnit.SECONDS);
                //c.await();
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + "---->" + e);
                //System.out.println(Thread.currentThread().getName() + "------>" + c.isBroken() + ", interrupted status:" + Thread.currentThread().isInterrupted());
            } finally {
                System.out.println(Thread.currentThread().getName() + " finishes!");
            }
        }
    }
}
