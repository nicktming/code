package com.futuretask;

import java.util.concurrent.Callable;

public class FutureTaskTest02 {
    public static void main(String[] args) throws Exception {
        FutureTask<String> futureTask = new FutureTask< >(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(Thread.currentThread().getName() + " starts to run.");
                Thread.sleep(5000);
                System.out.println(Thread.currentThread().getName() + " wakes up.");
                return "futurecall";
            }
        });

        Thread thread = new Thread(futureTask);

        thread.start();

        System.out.println(Thread.currentThread().getName() + " finished to start thread.");

        System.out.println(Thread.currentThread().getName() + "->" + futureTask.get());
    }
}
