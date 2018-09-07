package com.sourcecode.concurrencytools;

public class CountDownLatchTest2 {
    static CountDownLatch c = new CountDownLatch(1);
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            new MyThread().start();
        }
        c.countDown();
        System.out.println("main finished!");
    }

    static class MyThread extends Thread {
        public void run() {
            try {
                System.out.println(Thread.currentThread() + "----->before await");
                c.await();
                System.out.println(Thread.currentThread() + "----->after await");
            } catch (InterruptedException e) {
                System.out.println("in interrupted exception.");
            }
        }
    }

}
