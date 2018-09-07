package com.sourcecode.concurrencytools;

public class CountDownLatchTest {
    static CountDownLatch c = new CountDownLatch(2);
    public static void main(String[] args) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(1);
                c.countDown();
                System.out.println(2);
                //c.countDown();
                /**
                 *  打开注释 会依次打印1,2,3
                 *  关闭注释 会依次打印1,2 Main线程会阻塞在await()方法
                 */
            }
        }).start();
        c.await();
        System.out.println("3");
    }
}
