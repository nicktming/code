package com.sourcecode.concurrencytools_CyclicBarrier;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {
    private static class Generation {
        boolean broken = false;
    }
    /** 重入锁 */
    private final ReentrantLock lock = new ReentrantLock();
    /** 一个lock对象的Condition实例 */
    private final Condition trip = lock.newCondition();
    /** 拦截线程的总个数 */
    private final int parties;
    /** The command to run when tripped */
    private final Runnable barrierCommand;
    /** The current generation */
    private Generation generation = new Generation();
    /** 拦截线程的剩余需要数量 */
    private int count;

    public CyclicBarrier(int parties) {
        this(parties, null);
    }

    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen
        }
    }

    /**
     * @param timed 是否需要超时
     * @param nanos 时长
     * @return 返回还需要等待多少个线程才可以到达屏障
     * @throws InterruptedException 当前线程中断
     * @throws BrokenBarrierException 有其他线程中断或者其他线程超时
     * @throws TimeoutException 当前线程等待超时
     */
    private int dowait(boolean timed, long nanos)
            throws InterruptedException, BrokenBarrierException,
            TimeoutException {
        // 获取重入锁
        final ReentrantLock lock = this.lock;
        // 尝试获取锁
        lock.lock();
        try {

            //System.out.println(Thread.currentThread().getName() + " get locks.");

            // 获得当前代
            final Generation g = generation;

            // 如果有线程中断或者超时
            if (g.broken)
                throw new BrokenBarrierException();

            // 如果当前线程被中断
            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }

            int index = --count;
            //System.out.format("index=%d\n", index);
            if (index == 0) {  // 最后一个到达屏障的线程
                boolean ranAction = false;
                try {
                    final Runnable command = barrierCommand;
                    if (command != null)
                        command.run();
                    ranAction = true;
                    nextGeneration(); //更新下一代
                    return 0;
                } finally {
                    // 如果执行command.run发生异常,则breakBarrier
                    if (!ranAction)
                        breakBarrier();
                }
            }

            // loop until tripped, broken, interrupted, or timed out
            for (;;) {
                try {
                    if (!timed)
                        trip.await();
                    else if (nanos > 0L)
                        nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    // 如果等待过程中有被线程中断
                    if (g == generation && ! g.broken) {
                        breakBarrier();
                        throw ie;
                    } else {
                        // We're about to finish waiting even if we had not
                        // been interrupted, so this interrupt is deemed to
                        // "belong" to subsequent execution.
                        Thread.currentThread().interrupt();
                    }
                }

                // 如果当代的broken为true,表明有线程被中断
                if (g.broken)
                    throw new BrokenBarrierException();

                // 如果换代了 表示可以返回了
                if (g != generation)
                    return index;

                // 如果超时则先break the current generation
                // 再抛出超时异常
                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            // 释放锁
            //System.out.println(Thread.currentThread().getName() + " release locks.");
            lock.unlock();
        }
    }

    public int await(long timeout, TimeUnit unit)
            throws InterruptedException,
            BrokenBarrierException,
            TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    /**
     *  break the current generation
     *  1. broken设置为true
     *  2. count 重新设置为parties
     *  3. 唤醒所有线程
     */
    private void breakBarrier() {
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }

    /**
     *  start a new generation
     *  1. 唤醒所有等待中的线程
     *  2. count 重新设置为parties
     *  3. generation 设置成一个新的Generation对象
     */
    private void nextGeneration() {
        // signal completion of last generation
        trip.signalAll();
        // set up next generation
        count = parties;
        generation = new Generation();
    }


    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();   // break the current generation
            nextGeneration(); // start a new generation
        } finally {
            lock.unlock();
        }
    }

    public int getParties() {
        return parties;
    }

    public int getNumberWaiting() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return parties - count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return 当前代是否被破坏, 被破坏的两种情况, 某个线程中断或者等待超时
     */
    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

}
