package com.priorityblockingqueue;

import java.util.Comparator;
import java.util.Iterator;

public class Test01 {
    public static void main(String[] args) {
        PriorityBlockingQueue<Integer> pq = new PriorityBlockingQueue<>(10, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        pq.add(1);
        pq.add(3);
        pq.add(12);
        pq.add(4);
        pq.add(6);
        pq.add(17);
        pq.add(13);
        pq.add(8);
        pq.add(5);
        pq.add(10);
        pq.add(11);
        pq.add(19);
        pq.add(23);
        pq.add(14);

        System.out.println(pq);

        Iterator<Integer> iter = pq.iterator();
        while (iter.hasNext()) {
            int val = iter.next();
            if (val == 23) iter.remove();
            System.out.print(val + " ");
        }
    }
}
