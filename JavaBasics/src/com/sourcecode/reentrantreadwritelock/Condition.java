package com.sourcecode.reentrantreadwritelock;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface Condition {
    /**
     * 让当前线程休眠直到被唤醒或者被中断
     */
    void await() throws InterruptedException;

    //long awaitNanos(long nanosTimeout) throws InterruptedException;

    //boolean await(long time, TimeUnit unit) throws InterruptedException;

    /**
     * Causes the current thread to wait until it is signalled.
     */
    //void awaitUninterruptibly();

    /**
     * Causes the current thread to wait until it is signalled or interrupted,
     * or the specified deadline elapses.
     */
    //boolean awaitUntil(Date deadline) throws InterruptedException;

    /**
     *  唤醒当前Condtion对象中等待的一个线程
     */
    void signal();

    /**
     * 唤醒当前Condtion对象中等待的所有线程
     */
    void signalAll();
}
