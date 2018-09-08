package com.sourcecode.concurrencytools_CyclicBarrier;

import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
public class CyclicBarrierTest3 {
    static CyclicBarrier c = new CyclicBarrier(2);
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        Thread thread = new MyThread();
        thread.start();
        thread.interrupt();
        try {
            c.await();
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + "------>" + c.isBroken());
        }
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            try {
                c.await();
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + "------>" + c.isBroken());
            }
        }
    }
}
