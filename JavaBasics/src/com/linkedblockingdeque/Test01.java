package com.linkedblockingdeque;

import java.util.Iterator;

public class Test01 {
    public static void main(String[] args) {
        LinkedBlockingDeque<Integer> lbd = new LinkedBlockingDeque<>();
        lbd.addLast(5);
        Iterator iterator = lbd.iterator();
        System.out.println(iterator.hasNext());
        lbd.remove(5);
        System.out.println(iterator.next());

        Iterator iterator1 = lbd.iterator();
        System.out.println(iterator1.hasNext());
    }
}
