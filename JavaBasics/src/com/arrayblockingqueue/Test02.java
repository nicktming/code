package com.arrayblockingqueue;

import java.util.Iterator;

public class Test02 {
    public static void main(String[] args) {
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);
        queue.offer(1);
        queue.offer(2); // 放入2个元素
        Iterator<Integer> it1 = queue.iterator();
        Iterator<Integer> it2 = queue.iterator();
        Iterator<Integer> it3 = queue.iterator();
        Iterator<Integer> it4 = queue.iterator();
        Iterator<Integer> it5 = queue.iterator();
        while(it1.hasNext()) {
            System.out.print(it1.next()+",");
        }
        System.out.println();
        queue.poll();               // 清除第一个元素，再放入数据
        queue.offer(3);             // 填充一个元素，测试可以追上的情况，队列中有2,3
        while(it2.hasNext()) {
            System.out.print(it2.next()+",");
        }
        System.out.println();
        queue.poll();               // 再移除一个元素，此时1,2元素都移除了
        // 迭代器无法获取元素2,但是队列只相差一圈
        while(it3.hasNext()){
            System.out.print(it3.next()+",");
        }
        System.out.println();
        queue.offer(4);     // 队列中有3,4
        queue.poll();
        while(it4.hasNext()) {
            System.out.print(it4.next()+",");
        }
        System.out.println();
        queue.poll();
        queue.offer(5);
        while(it5.hasNext()) {
            System.out.print(it5.next()+",");
        }
    }
}
