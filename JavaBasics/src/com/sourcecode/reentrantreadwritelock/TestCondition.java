package com.sourcecode.reentrantreadwritelock;

import java.util.concurrent.TimeUnit;
public class TestCondition {
    public static void main(String[] args) throws InterruptedException {
        new MyThread().start();
        TimeUnit.SECONDS.sleep(5);
        lock.lock();
        System.out.println(getPrefix() + " gets lock");
        System.out.println(getPrefix() + "try to signal");
        con.signal();
        System.out.println(getPrefix() + "after signal");
        System.out.println(getPrefix() + " releases lock");
        lock.unlock();
    }

    static String getPrefix() {
        return Thread.currentThread().getName() + "==============";
    }

    static ReentrantLock lock = new ReentrantLock();
    static Condition con = lock.newCondition();

    static class MyThread extends Thread {
        public void run() {
            lock.lock();
            System.out.println(getPrefix() + "gets first lock");
            lock.lock();
            System.out.println(getPrefix() + "gets second lock");
            try {
                System.out.println(getPrefix() + "invoke await...");
                con.await();
                System.out.println(getPrefix() + "after await...");
                //TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println(getPrefix() + "release first lock");
                lock.unlock();
                System.out.println(getPrefix() + "release second lock");
                //lock.unlock();
                //System.out.println("release third lock");
            }
        }
    }
}
