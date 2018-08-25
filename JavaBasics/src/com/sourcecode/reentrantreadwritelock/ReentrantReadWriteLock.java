package com.sourcecode.reentrantreadwritelock;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class ReentrantReadWriteLock implements ReadWriteLock {
    private final ReentrantReadWriteLock.ReadLock readerLock;
    private final ReentrantReadWriteLock.WriteLock writerLock;
    final Sync sync;

    public ReentrantReadWriteLock() {
        this(false);
    }
    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    public void printWaitingNode() {
        sync.printQueue();
    }

    public ReentrantReadWriteLock.WriteLock writeLock() { return writerLock; }
    public ReentrantReadWriteLock.ReadLock  readLock()  { return readerLock; }

    abstract static class Sync extends AbstractQueuedSynchronizer {
        static final int SHARED_SHIFT   = 16;
        static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
        static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

        /** 返回c的高16位  读状态*/
        static int sharedCount(int c)    { return c >>> SHARED_SHIFT; }
        /** 返回c的低16位  写状态*/
        static int exclusiveCount(int c) { return c & EXCLUSIVE_MASK; }

        /**
         * 定义了一个包括重入数和线程id的类
         */
        static final class HoldCounter {
            int count = 0;
            // Use id, not reference, to avoid garbage retention
            final long tid = getThreadId(Thread.currentThread());
        }

        /**
         * 一个ThreadLocal的子类,value值是HoldCounter类的对象并重写了initValue()方法
         */
        static final class ThreadLocalHoldCounter
                extends ThreadLocal<HoldCounter> {
            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }

        /**
         * 一个thredlocal实例保存线程对应的HoldCount
         * 在构造函数或者readObject中完成初始化
         * 当读锁线程的重入数变为0时,会被removed.
         */
        private transient ThreadLocalHoldCounter readHolds;

        /**
         * 成功获取读锁的最后一个线程的HoldCounter对象.
         * 为了避免总是去readHolds中查找
         */
        private transient HoldCounter cachedHoldCounter;

        /**
         * firstReader是第一个获得读锁定的线程
         * 严格意义上是第一个使得状态值从0变为1的线程
         * firstReaderHoldCount是其对应的重入数
         *
         */
        private transient Thread firstReader = null;
        private transient int firstReaderHoldCount;

        /**
         *  构造函数, 初始化readHolds并设置状态
         */
        Sync() {
            readHolds = new ThreadLocalHoldCounter();
            setState(getState()); // ensures visibility of readHolds
        }

        /*
         * Acquires and releases use the same code for fair and
         * nonfair locks, but differ in whether/how they allow barging
         * when queues are non-empty.
         */

        /**
         * Returns true if the current thread, when trying to acquire
         * the read lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         */
        abstract boolean readerShouldBlock();

        /**
         * Returns true if the current thread, when trying to acquire
         * the write lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         */
        abstract boolean writerShouldBlock();

        /*
         * Note that tryRelease and tryAcquire can be called by
         * Conditions. So it is possible that their arguments contain
         * both read and write holds that are all released during a
         * condition wait and re-established in tryAcquire.
         */

        /**
         *
         * 作用: 写锁的释放
         * @param releases 释放的个数
         * @return 写锁是否完全释放 true 完全释放
         */
        protected final boolean tryRelease(int releases) {
            // 如果当前线程不是已经获取写锁的线程,则直接抛出异常
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int nextc = getState() - releases;
            boolean free = exclusiveCount(nextc) == 0;
            // 判断写锁(重入锁)是否已经全部释放完
            if (free)
                setExclusiveOwnerThread(null);
            setState(nextc); // 设置状态
            return free;
        }

        /**
         * 作用: 写锁的获取
         *
         * @param acquires 获取的个数
         * @return true表示获取锁, false表示未获取锁
         */
        protected final boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = getState();   // 整体状态
            int w = exclusiveCount(c); // 写状态的个数
            /**
             *  整体状态如果等于0 表明读锁和写锁目前都没有线程获取到 则可以去获取写锁
             *  如果不等于0
             *  1. 存在读锁或者当前线程不是已经获取写锁的线程,则直接返回
             *  2. 如果写锁的数量没有超过最高值则获得写锁
             */

            if (c != 0) {
                // (Note: if c != 0 and w == 0 then shared count != 0)
                // 存在读锁或者当前线程不是已经获取写锁的线程
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w + exclusiveCount(acquires) > MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // 重入式获取
                setState(c + acquires);
                return true;
            }
            /**
             *  表示整体状态为0
             *  如果writeShouldBlock需要阻塞或者CAS操作不成功则返回false
             */
            if (writerShouldBlock() ||
                    !compareAndSetState(c, c + acquires))
                return false;
            /**
             * 请注意setExclusiveOwnerThread该方法设置的是写锁
             */
            setExclusiveOwnerThread(current); // 设置当前线程是获得写锁的线程
            return true;
        }

        /**
         *  作用: 释放读锁
         * @param unused
         * @return
         */
        protected final boolean tryReleaseShared(int unused) {
            Thread current = Thread.currentThread(); // 获取当前线程

            /**
             *  1. firstReader == current 表明当前线程是那个第一个获得读锁的线程,可以直接操作firstReaderHolderCount就可以了
             *  2. 如果不是则看是不是最后一次获得读锁的线程,
             *      a. 如果不是则取出当前线程对应的holdcount,保存到rh中
             *      b. 如果是直接保存到rh
             *
             *      如果rh的count为1,表明当前线程获得读锁后没有重入过,既然是释放锁,这个时候就需要从threadlocal中删除掉
             *
             *      rh.count--
             *
             */
            if (firstReader == current) {
                // assert firstReaderHoldCount > 0;
                if (firstReaderHoldCount == 1)
                    firstReader = null;
                else
                    firstReaderHoldCount--;
            } else {
                HoldCounter rh = cachedHoldCounter;
                // 不是最后一个获得读锁的线程,需要从threadlocal中也就是readHolds中取出当前线程的HoldCount
                if (rh == null || rh.tid != getThreadId(current))
                    rh = readHolds.get();
                int count = rh.count;
                // count <= 1 需要从readHolds中删除, 进一步如果count<=0表明错误
                if (count <= 1) {
                    readHolds.remove();
                    if (count <= 0)
                        throw unmatchedUnlockException();
                }
                // 无论怎么样 rh已经是当前线程对应的HoldCount, 释放一个就是减少一个
                --rh.count;
            }
            // 循环操作更新状态, 如果读锁的个数为0,则表明所有读锁都释放完毕这个时候返回true.
            // 不然其他情况都是返回false.
            for (;;) {
                int c = getState();
                int nextc = c - SHARED_UNIT;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }

        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException(
                    "attempt to unlock read lock, not locked by current thread");
        }


        /**
         * @param unused 释放
         * @return  返回一个数值如果大于等于0,表明获得锁.
         *          返回一个数值如果小于0,表明没有获得锁.
         */
        protected final int tryAcquireShared(int unused) {
            Thread current = Thread.currentThread();
            int c = getState(); //获取当前状态
            if (exclusiveCount(c) != 0 &&
                    getExclusiveOwnerThread() != current)  //如果写锁存在并且写锁持有者不是当前线程
                return -1;
            // 说明 1.写锁不存在 或者 2.写锁存在但是写锁持有者是当前线程
            int r = sharedCount(c);  // 获取读锁的个数

            /**
             *   1. 读锁不需要阻塞.
             *   2. 读锁的总个数没有超过最大数.
             *   3. 通过CAS设置c的状态 因为高16位是读锁的个数 所以需要加上1<<16.
             */
            if (!readerShouldBlock() &&
                    r < MAX_COUNT &&
                    compareAndSetState(c, c + SHARED_UNIT)) {
                /**
                 *  上面三个条件都满足的情况下会进入这里继续执行
                 *  1.  r == 0 意味着当前线程是第一个获得读锁的线程(之前没有获得过).
                 *  2.  firstReader == current 意味当前线程是那个之前第一个获得读锁的线程 可以重入
                 *  3.  如果都不是就说明当前线程不是第一个获得读锁的线程,因此当前线程最起码是第二个获得读锁的线程,
                 *      a.  先去cachedHoldCounter看一下是不是最后一次获得读锁的线程,如果不是就把当前线程缓存起来
                 *          (因为此时该线程是目前最后一个获得读锁的线程)
                 *      b.  如果是的话如果rh.count==0,就需要把从readHolds中添加进去
                 *          (这是因为在对应的release中rh.count==0的时候readHolds做了清除操作)
                 *      rh.count++
                 *  返回1.
                 */
                if (r == 0) {
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader == current) {
                    firstReaderHoldCount++;
                } else {
                    HoldCounter rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current))
                        cachedHoldCounter = rh = readHolds.get();
                    else if (rh.count == 0)
                        readHolds.set(rh);
                    rh.count++;
                }
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        /**
         * 作用: 获取锁, 返回值大于等于0表示
         * 用于处理tryAcquireShared方法中未能满足的3个条件
         */
        final int fullTryAcquireShared(Thread current) {
            HoldCounter rh = null;
            for (;;) {
                int c = getState();                 // 获取当前状态
                if (exclusiveCount(c) != 0) {       // 如果写锁不为0 表明存在写锁
                    // 如果写锁不是当前线程(说明此刻已经有别的线程获得写锁了),则需要阻塞当前线程所以返回-1.
                    if (getExclusiveOwnerThread() != current)
                        return -1;
                    // else we hold the exclusive lock; blocking here
                    // would cause deadlock.
                    /**
                     *  这一段话的意思是如果当前线程如果在这里block了,那会形成死锁,
                     *  因为当前线程已经在持有写锁的情况来请求读锁的,那么该锁在没有释放锁的情况下block了
                     *  就会形成死锁了
                     */
                } else if (readerShouldBlock()) {  // 不存在写锁并且需要阻塞
                    // Make sure we're not acquiring read lock reentrantly
                    /**
                     * 确认一下当前线程有没有在之前获得锁,也就是在阻塞前确认一下不是重入读锁的线程
                     * 如果是重入锁的话就让他操作CAS 如果不是的话就需要阻塞
                     * 至于为什么,我个人理解如下:
                     *    对公平锁来说,readShouldBlock()返回true,表明AQS队列中有等待写锁的线程,
                     *    那么如果重入读锁也返回-1让其阻塞的话那就会形成死锁,因为该重入读锁由于阻塞无法释放读锁，
                     *    AQS等待队列中的写锁又因为读锁的存在而无法获得写锁从而形成死锁了.
                     */

                    if (firstReader == current) {  // 当前线程已经获得过锁则
                        // assert firstReaderHoldCount > 0;
                    } else {
                        if (rh == null) {
                            rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current)) {
                                rh = readHolds.get();
                                if (rh.count == 0)  // 计数为0, 需要从readHolds中删除
                                    readHolds.remove();
                            }
                        }
                        if (rh.count == 0) //说明当前线程之前没有获得锁
                            return -1;
                    }
                }
                // 如果读锁的个数达到最大值抛出error
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // CAS操作 逻辑跟tryAcquireShared方法里面的类似.
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (sharedCount(c) == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        if (rh == null)
                            rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter = rh; // cache for release
                    }
                    return 1;
                }
            }
        }

        /**
         * 尝试获取写锁,该方法给tryLock调用,返回false该线程也不会阻塞
         */
        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c != 0) {
                int w = exclusiveCount(c);
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
            }
            if (!compareAndSetState(c, c + 1))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }

        /**
         * 尝试获取读锁,该方法给tryLock调用,返回false该线程也不会阻塞
         */
        final boolean tryReadLock() {
            Thread current = Thread.currentThread();
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0 &&
                        getExclusiveOwnerThread() != current)
                    return false;
                int r = sharedCount(c);
                if (r == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (r == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        // Methods relayed to outer class
/*
        final ConditionObject newCondition() {
            return new ConditionObject();
        }
        */

        final Thread getOwner() {
            // Must read state before owner to ensure memory consistency
            return ((exclusiveCount(getState()) == 0) ?
                    null :
                    getExclusiveOwnerThread());
        }

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        final int getReadHoldCount() {
            if (getReadLockCount() == 0)
                return 0;

            Thread current = Thread.currentThread();
            if (firstReader == current)
                return firstReaderHoldCount;

            HoldCounter rh = cachedHoldCounter;
            if (rh != null && rh.tid == getThreadId(current))
                return rh.count;

            int count = readHolds.get().count;
            if (count == 0) readHolds.remove();
            return count;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         */
        private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            readHolds = new ThreadLocalHoldCounter();
            setState(0); // reset to unlocked state
        }

        final int getCount() { return getState(); }
    }

    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;
        // 写锁永远不需要阻塞
        final boolean writerShouldBlock() {
            return false; // writers can always barge
        }
        final boolean readerShouldBlock() {
            /* As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             */
            return apparentlyFirstQueuedIsExclusive();
        }
    }

    static final class FairSync extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;
        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }
        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
        // 如果前面有节点 返回true 说明需要阻塞
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#readLock}.
     */
    public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        /**
         * 构造函数
         *
         * @param lock ReentrantReadWriteLock类的实例,主要是为了获取lock.sync
         * @throws NullPointerException 如果lock为null 运行时异常
         */
        protected ReadLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * 获取读锁.
         */
        public void lock() {
            sync.acquireShared(1);
        }


        public void lockInterruptibly() throws InterruptedException {
            //sync.acquireSharedInterruptibly(1);
        }

        public boolean tryLock() {
            return sync.tryReadLock();
        }

        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            //return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
            return true;
        }

        public void unlock() {
            sync.releaseShared(1);
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() +
                    "[Read locks = " + r + "]";
        }
    }

    public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        /**
         * 构造函数
         *
         * @param lock ReentrantReadWriteLock类的实例,主要是为了获取lock.sync
         * @throws NullPointerException 如果lock为null 运行时异常
         */
        protected WriteLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * 获取写锁
         */
        public void lock() {
            sync.acquire(1);
        }

        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        public boolean tryLock( ) {
            return sync.tryWriteLock();
        }

        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        public void unlock() {
            sync.release(1);
        }

        public Condition newCondition() {
            //return sync.newCondition();
            return null;
        }
        public String toString() {
            Thread o = sync.getOwner();
            return super.toString() + ((o == null) ?
                    "[Unlocked]" :
                    "[Locked by thread " + o.getName() + "]");
        }
        public boolean isHeldByCurrentThread() {
            return sync.isHeldExclusively();
        }

        public int getHoldCount() {
            return sync.getWriteHoldCount();
        }
    }

    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    protected Thread getOwner() {
        return sync.getOwner();
    }

    public int getReadLockCount() {
        return sync.getReadLockCount();
    }

    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }

    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }

    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }

    public int getReadHoldCount() {
        return sync.getReadHoldCount();
    }

    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    public String toString() {
        int c = sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);

        return super.toString() +
                "[Write locks = " + w + ", Read locks = " + r + "]";
    }

    static final long getThreadId(Thread thread) {
        return UNSAFE.getLongVolatile(thread, TID_OFFSET);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long TID_OFFSET;
    static {
        try {
            //UNSAFE = sun.misc.Unsafe.getUnsafe();

            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe)f.get(null);

            Class<?> tk = Thread.class;
            TID_OFFSET = UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("tid"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
