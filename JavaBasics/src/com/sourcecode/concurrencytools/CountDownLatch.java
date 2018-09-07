package com.sourcecode.concurrencytools;

import com.sourcecode.reentrantreadwritelock.AbstractQueuedSynchronizer;
import java.util.concurrent.TimeUnit;

public class CountDownLatch {
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        // 返回当前AQS的状态值
        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            // 其实跟传入的参数acquires没有什么实质的作用
            // 根据当前AQS的状态值是否为0,如果为0就获得锁,如果不为0会进入到AQS中的acquireSharedInterruptibly方法中
            // 具体的操作需要了解AQS
            return (getState() == 0) ? 1 : -1;
        }

        // 释放 逻辑非常简单
        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }
    private final Sync sync;
    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }
    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }
    public void countDown() {
        sync.releaseShared(1);
    }
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
