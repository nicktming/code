package com.sourcecode.concurrencytools_Semaphore;

import java.util.concurrent.TimeUnit;
public class SemaphoreTest3 {
    static Semaphore semaphore = new Semaphore(3);
    public static void main(String[] args) throws InterruptedException {
        MyThread thread1 = new MyThread();
        thread1.start();
        TimeUnit.SECONDS.sleep(2);
        semaphore.acquire();
        System.out.println(Thread.currentThread().getName() + " get locks.");
        semaphore.release();
        System.out.println(Thread.currentThread().getName() + " finishes");
    }

    static class MyThread extends Thread {
        public void run() {
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " get Locks 1");
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " get Locks 2");
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " get Locks 3");
                TimeUnit.SECONDS.sleep(10);
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " release locks 1");
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " release locks 2");
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " release locks 3");
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
