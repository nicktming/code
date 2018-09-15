package com.sourcecode.atomic_AtomicInteger;

public class AtomicIntegerArrayTest {
    static int[] value = new int[] { 1, 2, 3, 4, 5};
    static AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(value);
    public static void main(String[] args) {
        for (int i = 0; i < value.length; i++) {
            atomicIntegerArray.compareAndSet(i, value[i], -1);
        }
        System.out.print("new value:");
        for (int i = 0; i < value.length; i++) {
            System.out.print(atomicIntegerArray.get(i) + ",");
        }
        System.out.print("\norigin array:");
        for (int i = 0; i < value.length; i++) {
            System.out.print(value[i] + ",");
        }
    }
}