package com.sourcecode.atomic_AtomicInteger;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;


    // setup to use Unsafe.compareAndSwapInt for updates
    //private static final Unsafe unsafe = Unsafe.getUnsafe();  官方源码
    private static Unsafe unsafe = null; // 为了自己获得unsafe
    private static final long valueOffset;

    static {
        try {
            /**
             *  自己通过反射获得unsafe
             */
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe)f.get(null);

            valueOffset = unsafe.objectFieldOffset
                    (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    // 设置为volatile属性
    private volatile int value;

    // 构造函数初始化
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }
    public AtomicInteger() {
    }

    public final int get() {
        return value;
    }
    /** 设置value值 */
    public final void set(int newValue) {
        value = newValue;
    }
    /**
     * 最终会设置成newValue，使用lazySet设置值后，可能导致其他
     * 线程在之后的一小段时间内还是可以读到旧的值
     */
    public final void lazySet(int newValue) {
        unsafe.putOrderedInt(this, valueOffset, newValue);
    }

    /**
     *  原子的方式设置新的值
     *  返回旧的值
     */
    public final int getAndSet(int newValue) {
        return unsafe.getAndSetInt(this, valueOffset, newValue);
    }
    /**
     *  如果输入的数值等于预期值，则以原子方
     *  式将该值设置为输入的值。
     */
    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
    public final boolean weakCompareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
    /**
     *  原子的方式设置旧的值加1
     *  返回旧的值
     */
    public final int getAndIncrement() {
        return unsafe.getAndAddInt(this, valueOffset, 1);
    }
    /**
     *  原子的方式设置旧的值减1
     *  返回旧的值
     */
    public final int getAndDecrement() {
        return unsafe.getAndAddInt(this, valueOffset, -1);
    }
    /**
     *  原子的方式设置旧的值加delta
     *  返回旧的值
     */
    public final int getAndAdd(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }
    /**
     *  原子的方式设置旧的值加1
     *  返回新的值
     */
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }
    /**
     *  原子的方式设置旧的值减1
     *  返回新的值
     */
    public final int decrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, -1) - 1;
    }
    /**
     *  原子的方式设置旧的值加delta
     *  返回新的值
     */
    public final int addAndGet(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta) + delta;
    }

    /**
     *  for循环操作设置新值为
     *  应用于updateFunction后的值
     *  返回旧的值
     */
    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }
    /**
     *  for循环操作设置新值为
     *  应用于updateFunction后的值
     *  返回新的值
     */
    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }
    /**
     *  for循环操作设置新值为
     *  应用于accumulatorFunction后的值
     *  返回旧的值
     */
    public final int getAndAccumulate(int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }
    /**
     *  for循环操作设置新值为
     *  应用于accumulatorFunction后的值
     *  返回新的值
     */
    public final int accumulateAndGet(int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }
    public String toString() {
        return Integer.toString(get());
    }
    public int intValue() {
        return get();
    }
    public long longValue() {
        return (long)get();
    }
    public float floatValue() {
        return (float)get();
    }
    public double doubleValue() {
        return (double)get();
    }
}
