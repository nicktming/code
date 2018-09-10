package com.sourcecode.concurrencytools_CyclicBarrier;

public class TestException {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new MyThread();
        thread.start();
        thread.interrupt();
    }

    static class MyThread extends Thread {
        public void run() {
            try {
                test();
            } catch(InterruptedException ie) {
                    System.out.println(Thread.currentThread() + " get exception.");
            }
        }
    }

    public static void test() throws InterruptedException {
        try {
            try {
                Thread.sleep(5000);
            } catch(InterruptedException e) {
                System.out.println("in exception");
                throw e;
            }
        } finally {
            System.out.println("in finally");
        }
    }
}
