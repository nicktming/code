package com.sourcecode.reentrantreadwritelock;

import java.util.concurrent.TimeUnit;

public class TestCondition {
    public static void main(String[] args) throws InterruptedException {
        new MyThread().start();
        TimeUnit.SECONDS.sleep(5);
        lock.lock();
        System.out.println(Thread.currentThread().getName() + " gets lock");
        System.out.println("try to signal");
        con.signal();
        System.out.println("after signal");
        System.out.println(Thread.currentThread().getName() + " releases lock");
        lock.unlock();
    }

    static ReentrantLock lock = new ReentrantLock();
    static Condition con = lock.newCondition();

    static class MyThread extends Thread {
        public void run() {
            lock.lock();
            System.out.println("gets first lock");
            lock.lock();
            System.out.println("gets second lock");
            try {
                System.out.println("invoke await...");
                con.await();
                System.out.println("after await...");
                //TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println("release first lock");
                lock.unlock();
                System.out.println("release second lock");
                //lock.unlock();
                //System.out.println("release third lock");
            }
        }
    }
}
