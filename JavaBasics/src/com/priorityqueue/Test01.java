package com.priorityqueue;

import java.util.Comparator;

public class Test01 {
    public static void main(String[] args) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(10, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        pq.add(1);
        pq.add(3);
        pq.add(2);
        pq.add(0);
        while (!pq.isEmpty()) {
            System.out.println(pq.poll());
        }
    }
}
