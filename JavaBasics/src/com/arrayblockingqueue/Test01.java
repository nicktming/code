package com.arrayblockingqueue;

public class Test01 {

    static ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue(10);

    public static void main(String[] args) {
        Thread consumer = new Consumer("consumer01");
        Thread producer = new Producer("producer01");
        producer.start();
        consumer.start();
    }

    static class Consumer extends Thread {
        Consumer(String name) {super(name);}
        public void run() {
            try {
                consumer();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Producer extends Thread {
        Producer(String name) {super(name);}
        public void run() {
            try {
                producer();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void producer() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            arrayBlockingQueue.put(Thread.currentThread().getName() + "-" + i);
        }
        System.out.println("producer finished!");
    }

    private static void consumer() throws InterruptedException {
        while(true) {
            System.out.println(Thread.currentThread().getName() + " gets " + arrayBlockingQueue.take());
        }
    }
}
