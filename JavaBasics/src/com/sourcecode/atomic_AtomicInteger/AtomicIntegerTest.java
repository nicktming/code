package com.sourcecode.atomic_AtomicInteger;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class AtomicIntegerTest {
    static AtomicInteger ai = new AtomicInteger(1);
    public static void main(String[] args) {
        System.out.println(ai.getAndIncrement());
        System.out.println(ai.get());

        IntBinaryOperator accumulatorFunction = (x, y) -> x + y;
        System.out.println(ai.accumulateAndGet(10, accumulatorFunction));

        IntUnaryOperator updateFunction = x -> x * x;
        System.out.println(ai.updateAndGet(updateFunction));
    }
}
