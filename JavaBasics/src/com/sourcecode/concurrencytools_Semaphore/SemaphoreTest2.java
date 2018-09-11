package com.sourcecode.concurrencytools_Semaphore;

import java.util.concurrent.TimeUnit;
public class SemaphoreTest2 {

    static Semaphore semaphore = new Semaphore(1);

    public static void main(String[] args) throws InterruptedException {
        MyThread myThread = new MyThread();
        myThread.start();
        MyThread myThread2 = new MyThread();
        myThread2.start();
        TimeUnit.SECONDS.sleep(2);
        myThread2.interrupt();
    }

    static class MyThread extends Thread {
        public void run() {
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " get Semaphore");
            } catch (InterruptedException ie) {
                System.out.println(Thread.currentThread().getName() + " semaphore.acquire, ie:" + ie);
            } finally {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ie) {
                    System.out.println(Thread.currentThread().getName() + "wait 10s, ie:" + ie);
                }
                System.out.println(Thread.currentThread().getName() + " release Semaphore.");
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " available:" + semaphore.availablePermits());
            }
        }
    }
}
