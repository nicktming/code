package com.futuretask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class FutureTaskTest01 {
    static final int COUNT = 1000000000;
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

        futureTask.cancel(true);

        try {
            System.out.println(Thread.currentThread().getName() + "->" + futureTask.get() + "<-");
        } catch (ExecutionException ex) {
            System.out.println("ExecutionException exception:" + ex);
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException exception:" + ex);
        } catch (Exception ex) {
            System.out.println("Exception exception:" + ex);
        }
        System.out.println(Thread.currentThread().getName() + " finished!");
    }
}
