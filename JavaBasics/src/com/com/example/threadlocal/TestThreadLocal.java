package com.com.example.threadlocal;

import java.util.concurrent.TimeUnit;
public class TestThreadLocal {

    static ThreadLocal<Integer> count = new ThreadLocal<Integer>(){
        protected Integer initialValue() {
            return 100;
        }
    };

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            new Thread(new Runner(), "thread-" + i).start();
            TimeUnit.SECONDS.sleep(1);
        }
    }

    static class Runner implements Runnable {
        public void run() {
            for (int i = 0; i < 3; i++) {
                count.set(count.get() + 1);
                System.out.println(Thread.currentThread().getName() + ":" +
                        count.get());
            }
        }
    }
}
