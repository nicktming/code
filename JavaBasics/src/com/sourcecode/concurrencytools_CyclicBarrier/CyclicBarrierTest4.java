package com.sourcecode.concurrencytools_CyclicBarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
public class CyclicBarrierTest4 {
    static CyclicBarrier c = new CyclicBarrier(5);
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        for (int i = 0; i < 5; i++) {
            Thread thread = new MyThread();
            thread.start();
        }
        System.out.println(Thread.currentThread().getName() + "------>finishes!");
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            try {
                c.await();
            } catch (Exception e) {
                System.out.println(e);
                System.out.println(Thread.currentThread().getName() + "------>" + c.isBroken() + ", interrupted status:" + Thread.currentThread().isInterrupted());
            }
        }
    }
}
