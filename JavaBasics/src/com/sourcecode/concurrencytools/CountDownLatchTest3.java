package com.sourcecode.concurrencytools;

import java.util.concurrent.TimeUnit;

public class CountDownLatchTest3 {
    static CountDownLatch c = new CountDownLatch(1);
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new MyThread();
        thread.start();
        TimeUnit.SECONDS.sleep(1);
        thread.interrupt();
        //c.countDown();
        System.out.println(Thread.currentThread() + "----->finished!");
    }

    static class MyThread extends Thread {
        public void run() {
            try {
                System.out.println(Thread.currentThread() + "----->before await");
                c.await();
                System.out.println(Thread.currentThread() + "----->after await");
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread() + "----->in interrupted exception.");
            }
            System.out.println(Thread.currentThread() + "----->finished!");
        }
    }
}
