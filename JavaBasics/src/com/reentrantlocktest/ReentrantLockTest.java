package com.reentrantlocktest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest implements Runnable{
    public static ReentrantLock lock = new ReentrantLock();
    public static int i = 0;

    public void run() {
        for (int j = 0; j < 10000; j++) {
            lock.lock();  // 看这里就可以
            //lock.lock(); ①
            try {
                i++;
                System.out.println(Thread.currentThread().getName() + " get lock");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                System.out.println(Thread.currentThread().getName() + " release lock");
                lock.unlock(); // 看这里就可以

                //lock.unlock();②
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReentrantLockTest test = new ReentrantLockTest();
        Thread t1 = new Thread(test);
        Thread t2 = new Thread(test);
        t1.start();t2.start();
        t1.join(); t2.join(); // main线程会等待t1和t2都运行完再执行以后的流程
        System.err.println(i);
    }
}