package com.usafe;

import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class TestField {
    private int age;
    private String name;
    private static int value;

    public static void main(String[] args) throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);

        long setoff = unsafe.objectFieldOffset(TestField.class.getDeclaredField("age"));
        System.out.println("setoff:" + setoff);

        TestField test1 = new TestField();
        test1.age = 10;
        TestField test2 = new TestField();
        test2.age = 10;

        unsafe.compareAndSwapInt(test1,  setoff, 10, 11);
        unsafe.compareAndSwapInt(test2,  setoff, 10, 12);
        System.out.println("test1 age:" + test1.age);
        System.out.println("test2 age:" + test2.age);
    }
}
