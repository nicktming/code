package com.sourcecode.reentrantreadwritelock;

public class TestCache {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new Thread(new Runner(), "thread-" + i).start();
        }
        for (int i = 0; i < 10; i++) {
            SleepUnit.sleep(5);
            Cache.rwl.printWaitingNode();
        }
    }

    static class Runner implements Runnable {
        public void run() {
            Cache.put("k0", "k0");
        }
    }
}
