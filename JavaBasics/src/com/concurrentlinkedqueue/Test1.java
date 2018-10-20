package com.concurrentlinkedqueue;

import java.util.Iterator;

public class Test1 {
    public static void main(String[] args) {
        test1();
    }

    public static void test1 () {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();
        queue.add("A");
        queue.add("B");
        Iterator<String> iter = queue.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

}
