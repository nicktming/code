package com.linkedblockingqueue;

public class Test01 {

    static LinkedBlockingQueue lbq = new LinkedBlockingQueue(5);

    public static void main(String[] args) {
        Consumer consumer01 = new Consumer("consumer01");
        Consumer consumer02 = new Consumer("consumer02");
        Producer producer01 = new Producer("producer01");
        Producer producer02 = new Producer("producer02");
        Producer producer03 = new Producer("producer03");
        consumer01.start();
        consumer02.start();
        producer01.start();
        producer02.start();
        producer03.start();
    }

    static class Consumer extends Thread {
        Consumer(String name) {super(name);}
        public void run() {
            try {
                while (true) {
                    System.out.println(Thread.currentThread().getName() + " gets " + lbq.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Producer extends Thread {
        Producer(String name) {super(name);}
        public void run() {
            try {
                for (int i = 0; i <3; i++) {
                    lbq.put(Thread.currentThread().getName() + "-" + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
