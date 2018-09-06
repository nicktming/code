package com.sourcecode.reentrantreadwritelock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.LockSupport;
import sun.misc.Unsafe;

public class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer 
			implements java.io.Serializable {
	
	private static final long serialVersionUID = 7373984972572414691L;
	
	protected AbstractQueuedSynchronizer() { }
	
	static final class Node {
		// 共享
        static final Node SHARED = new Node();
        // 表示独占
        static final Node EXCLUSIVE = null;
        // 表明这个节点已经被取消
        static final int CANCELLED =  1;
        // 表明需要唤醒下一个等待的线程
        static final int SIGNAL    = -1;
        // 后续会讨论
        static final int CONDITION = -2;
        // 后续会讨论
        static final int PROPAGATE = -3;
        // 当前节点的等待状态
        volatile int waitStatus;
        // 当前节点的前一个节点
        volatile Node prev;
        // 当前节点的下一个节点
        volatile Node next;
        // 当前节点所表示的线程
        volatile Thread thread;

		// Condition条件队列中会用到
		Node nextWaiter;
		
		//判断是独占还是共享
		final boolean isShared() {
            return nextWaiter == SHARED;
        }
		// 取前一个节点
		final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }
		// 无参构造函数
		Node() {}
		
		Node(Thread thread, Node mode) {   
            this.nextWaiter = mode;
            this.thread = thread;
        }
		
		Node(Thread thread, int waitStatus) {
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
		
		public String toString() {
			return "[" + (thread == null ? "NULL" : (thread.getName() + "," +  (isShared() ? "共享" : "独占") )) + "," + waitStatus + "]";
		}
		
	}
	// 同步器等待队列的头节点
	private transient volatile Node head;
	// 同步器等待队列的尾节点
	private transient volatile Node tail;
	// 同步器的状态值
	private volatile int state;
	// 获取同步器状态值
	protected final int getState() {
        return state;
    }
	// 设置同步器的状态值
	protected final void setState(int newState) {
        state = newState;
    }
	
	public final boolean compareAndSetState(int expect, int update) {
		return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
	}


	public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        /** First node of condition queue. */
        private transient Node firstWaiter;
        /** Last node of condition queue. */
        private transient Node lastWaiter;

        public ConditionObject() {}


        /**
         * Implements interruptible condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled or interrupted.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted()) // 如果当前线程被中断了 抛出异常
                throw new InterruptedException();
            Node node = addConditionWaiter();  // 添加一个新的节点到条件等待队列并返回该节点
            int savedState = fullyRelease(node); // 释放锁, 因为可能是重入锁,需要用fullRelease
            int interruptMode = 0;
            /**
             *  有两个条件退出while循环
             *  1. 当前节点出现在同步等待队列中
             *  2. 如果线程有中断(中断状态被改变)
             */
            while (!isOnSyncQueue(node)) {
                /**
                 *  线程休眠 有两种方式会从park方法退出
                 *  1. 被其他线程unpark唤醒
                 *  2. 线程发生中断
                 */
                LockSupport.park(this);
                // 如果线程有中断(中断状态被改变) 则break退出循环
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            // 尝试获得锁, 参数为之前释放锁的个数(在重入锁中可以理解为重入的次数)
            // acquireQueued(node, savedState) 返回true表示在获得锁的过程中线程有中断过
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        /** Mode meaning to reinterrupt on exit from wait */
        private static final int REINTERRUPT =  1;
        /** Mode meaning to throw InterruptedException on exit from wait */
        private static final int THROW_IE    = -1;

        /**
         * 检查中断
         * 返回THROW_IE       如果中断发生在被别的线程调用signal之前
         * 返回REINTERRUPT    如果中断发生在被别的线程调用signal之后
         * 返回0              如果没有发生中断
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                    (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                    0;
        }

        /**
         * Throws InterruptedException, reinterrupts current thread, or
         * does nothing, depending on mode.
         * 判断是否需要抛出异常
         */
        private void reportInterruptAfterWait(int interruptMode)
                throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * 作用: 添加一个新的等待节点到条件等待队列(Condition wait queue)
         * @return 返回新生成的等待节点
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // 如果最后一个等待节点不为空但是不为Condition(意味着被取消了)
            // 清除该节点并且重新设置最后一个等待节点
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            // 生成一个新的节点 节点类型是Node.CONDITION
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**
         * 作用: 把条件等待队列中等待最长的线程所对应的节点(也就是头节点)转移到同步等待队列中
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        public final void signal() {
            if (!isHeldExclusively()) // 如果该线程没有获得锁则抛出异常
                throw new IllegalMonitorStateException();
            Node first = firstWaiter; // 获得条件等待队列中的第一个节点,也就是等待时间最长的节点
            if (first != null)
                doSignal(first); // 唤醒first节点
        }

        /**
         * 作用: 把条件等待队列中所有节点转移到同步等待队列中
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        /**
         *  取消在条件等待队列中已经被取消的节点
         *  就是一个简单的链表遍历的过程
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        /**
         * 作用: 把first节点从条件等待队列转移到同步等待队列,
         *       如果失败会尝试转移条件等待队列中的下一个节点,直到条件等待队列为空.
         * @param first
         */
        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null) //更新firstWaiter节点
                    lastWaiter = null;   // 当条件等待队列为空的时候更新lastWaiter节点
                first.nextWaiter = null; // 从条件等待队列中删除该节点
            } while (!transferForSignal(first) &&
                    (first = firstWaiter) != null);
            // 如果把first节点成功转移到同步等待队列中或者条件等待队列为空才会退出循环
        }

        /**
         * 作用: 把条件等待队列中的每个节点依次转移到同步等待队列中
         */
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * 返回条件等待队列中是否有线程在等待
         * true 不是有线程在等待
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        /**
         * Returns true if this condition was created by the given
         * synchronization object.
         *
         * @return {@code true} if owned
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         *
         * @return 条件等待队列中线程的个数
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * 返回条件等待队列中的线程
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    /**
     * 把一个条件等待队列的节点转移到同步队列
     * 返回ture如果成功, 失败的话说明该节点已经被取消
     * @param node the node
     * @return true if successfully transferred (else the node was
     * cancelled before signal)
     */
    final boolean transferForSignal(Node node) {
        /*
         * If cannot change waitStatus, the node has been cancelled.
         * 如果不能改变waitStatus,说明这个节点已经被取消了
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         * 如果前驱节点取消或尝试设置waitStatus失败，唤醒重新同步,(因为此时休眠可能会造成没有线程来唤醒)
         * 唤醒的过程中如果获得锁失败会调用shouldParkAfterFailedAcquire保证线程休眠后会有线程唤醒
         */
        Node p = enq(node); // 加入到同步等待队列并且返回该节点在同步等待队列中的前一个节点
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * Invokes release with current state value; returns saved state.
     * Cancels node and throws exception on failure.
     * @param node the condition node for this wait
     * @return previous sync state 返回之前的同步状态
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            // 节点状态 持有锁的数量
            int savedState = getState();
            // 释放锁
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                // 如果释放锁失败 会抛出异常 必须是先获得锁 才可以调用wait
                throw new IllegalMonitorStateException();
            }
        } finally {
            // 如果失败的话修改节点的waitStatus为取消状态
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    /**
     * 判断该节点是否转移到同步队列中
     * @param node the node
     * @return true if is reacquiring
     */
    final boolean isOnSyncQueue(Node node) {
        // 如果状态是CONDITION 或者 前驱节点为null 则表明肯定不在同步队列中 直接返回false.
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        // 此时node的状态不等于CONDITION并且前驱节点不为null
        // 如果node.next != null 则肯定在同步队列中
        if (node.next != null) // If has successor, it must be on queue
            return true;
        /*
         * 如果node的后驱节点是null,担心CAS失败,还是扫描确保
         */
        return findNodeFromTail(node);
    }

    /**
     * 从后往前扫描链表查找node是否在sync queue上.
     * @return true if present
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }


    /**
     * Transfers node, if necessary, to sync queue after a cancelled wait.
     * Returns true if thread was cancelled before being signalled.
     *
     * @param node the node
     * @return true if cancelled before the node was signalled
     */
    final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        /*
         * If we lost out to a signal(), then we can't proceed
         * until it finishes its enq().  Cancelling during an
         * incomplete transfer is both rare and transient, so just
         * spin.
         */
        while (!isOnSyncQueue(node))
            Thread.yield(); //线程让步
        return false;
    }
	
	
	/**
	 *  添加addWaiter方法
     *  返回node节点在同步队列的前一个节点
	 */
	
	// 将node节点放到等待队列的尾部
	private Node enq(final Node node) {
		/**
		 * 无限循环到条件满足后退出
		 */
        for (;;) {
            Node t = tail;
            /**
             * 如果队列尾部是空 表明队列还没有进行过初始化
             * 如果不为空 则把节点链接到队列的尾部
             */
            if (t == null) {
            		//生成一个thread为null的节点并且设置为头节点
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
            		//利用unsafe的操作把当前节点设置到尾部
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
	
	//添加一个节点到当前队列 在尾部添加
	private Node addWaiter(Node mode) {
		// 生成一个mode类型的节点 节点的thread是当前线程
        Node node = new Node(Thread.currentThread(), mode);
        /**
         * 如果等待队列尾部不为空,则直接通过unsafe操作放到队列尾部
         * 如果不是则调用 enq方法把节点插入到等待队列中.
         */
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }
	
	/**
	 * 添加tryAcquire(int arg) 方法
	 */
	
	protected boolean tryAcquire(int arg) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 *  添加acquireQueued方法
	 */
	
	/**
	 * 
	 * @param node 当前节点
	 * @param arg acquire请求的状态参数
	 * @return 返回是否需要对当前运行线程进行中断
	 */
	
	final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
	
	/**
	 * 作用: 把node节点设置为等待队列的头节点
	 */
	private void setHead(Node node) {
		/**
		 * 1. 把当前节点设置为头节点
		 * 2. 把当前节点的thread设置为null,因为这个node.thread已经获得了锁
		 * 3. 把当前节点的前链接设置为null
		 */
        head = node;
        node.thread = null;
        node.prev = null;
    }
	
	/**
	 * 这个方法必须要保证pred == node.prev
	 * 作用: 此方法是在判断node节点是否可以进行休眠状态, 因为进行休眠状态可以节约资源利用(cpu)
	 *      所以需要让node休眠前要确保有节点可以唤醒它,因此下面所做的事情就是检查等待队列中
	 *      前面的节点是否满足条件.
	 */
	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /**
             * 如果节点pred的状态是Node.SIGNAL的时候,表明pred在release的时候
             * 会唤醒下一个节点,也就是node节点
             * 返回true 表明node节点可以休眠
             */
            return true;
        if (ws > 0) {
            /**
             * 表明节点pred所对应的线程已经被取消了,因此需要一直往前找
             * 直到前一个节点的waitStatus小于等于0,中间的那些取消的节点
             * 会被删除 因为最后pred.next = node.
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /**
             * 所以这个分支表示当前节点的等待状态要么是0或者PROPAGATE,
             * 此时直接把它设置为SIGNAL
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
	
	/**
	 * 作用: 让当前线程进行休眠状态并且在唤醒或者中断后返回中断状态
	 * 注意: 第一句就会让当前线程进行休眠状态,只有等到当前线程被唤醒后才可以进入第二句话
	 *      线程被唤醒有两种情况:
	 *      1. 被前面的(SIGNAL)节点唤醒 此时第二句话返回false
	 *      2. 线程被中断 此时第二句话返回true (关于中断的话题会有另外专门博客介绍)
	 */
	private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }
	
	
	/**
	 * 添加cancelAcquire方法
	 * 作用: 取消该节点
	 */
	private void cancelAcquire(Node node) {
        // 如果节点为null 直接返回
        if (node == null)
            return;
        // 设置节点所对应的thread为null
        node.thread = null;

        // 找到node的前驱节点(必须是没有被取消的节点)
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // 前驱节点的next节点
        Node predNext = pred.next;

        // 设置当前节点为取消状态
        node.waitStatus = Node.CANCELLED;

        /**
         * 如果当前节点是尾节点: (表明当前节点可以直接取消,不用管后面的节点,因为后面没有节点了)
         * 		 1. 设置前驱节点pred成为新的尾节点
         *       2. 在1成功的条件下设置尾节点的next为null
         */
        if (node == tail && compareAndSetTail(node, pred)) {
        	//情况3
            compareAndSetNext(pred, predNext, null);
        } else {
        		/**
        		 * 表明node后面还有节点,因此后面的节点会需要唤醒
        		 * 有两种情况:
        		 *     1. 前驱节点不是头节点,并且前驱节点的等待状态是SINGAL或者可以成为SINGAL状态,
        		 *        并且前驱节点的thread不能为null. 这种情况下不需要去唤醒node后面的线程,因为pred节点可以去唤醒的
        		 *     2. 如果不符合1,就需要主动去唤醒node后面的节点线程了,因为此时不唤醒,node后面节点的线程就没办法再唤醒了
        		 */
            int ws;
            //情况1 
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
            		//情况2
                unparkSuccessor(node);
            }
            //设置node节点自旋
            node.next = node; // help GC
        }
    }
	
	/**
	 *  唤醒node的后驱节点(node后面第一个没有被取消的节点)
	 */
	private void unparkSuccessor(Node node) {
        /*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         */
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        /*
         * 取得node的后继节点
         * 
         */
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }
	
	static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }
	
	
	public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
	
	static final long spinForTimeoutThreshold = 1000L;
	
	private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
	
	public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }
	
	private void doAcquireInterruptibly(int arg)
	        throws InterruptedException {
	        final Node node = addWaiter(Node.EXCLUSIVE);
	        boolean failed = true;
	        try {
	            for (;;) {
	                final Node p = node.predecessor();
	                if (p == head && tryAcquire(arg)) {
	                    setHead(node);
	                    p.next = null; // help GC
	                    failed = false;
	                    return;
	                }
	              /**
	               * 当判断是被中断而不是被唤醒的时候,抛出InterruptedException
	               * 
	               */
	                if (shouldParkAfterFailedAcquire(p, node) &&
	                    parkAndCheckInterrupt())
	                    throw new InterruptedException();  
	            }
	        } finally {
	            if (failed)
	                cancelAcquire(node);
	        }
	    }
	
	public final void acquireInterruptibly(int arg)
            throws InterruptedException {
		/**
		 * 如果当前线程已经被中断了 直接抛出InterruptedException
		 * 注意:Thread.interrupted()会在复位当前线程的中断状态 也就是变为false
		 */
        if (Thread.interrupted())  
            throw new InterruptedException();
        // 尝试获取锁 如果获取不到则加入到阻塞队列中
        if (!tryAcquire(arg))  
            doAcquireInterruptibly(arg);
    }
	
	protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }
	
	public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
	
	
	/**
	 *   共享锁的代码段
	 */
	
	
	private void doReleaseShared() {
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
            		/**
            		 * 进入到这里表明队列不为空并且有后继节点
            		 */
                int ws = h.waitStatus;
                // 只有当节点状态是SIGNAL的时候才会去唤醒后继节点 
                // 并且把节点状态改为0
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // 进入for循环重试
                    unparkSuccessor(h);
                }
                // 如果状态是0 则更新为PROPAGATE状态
                // 因为只有状态是-1的时候才要去唤醒
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // 进入for循环重试
            }
            /**
             *  为什么还要判断 h == head 呢？
             *  就说明在执行该方法的时候, head有可能会发生改变
             *  这是因为在执行上面的unparkSuccessor(h)的时候会去唤醒后驱节点
             *  现在设置后驱节点对应的线程为thread-B
             *  此方法所在的线程是thread-A
             *  如果thread-A在执行完unparkSuccessor(h)失去控制权,这个时候thread-B
             *  刚刚好从parkAndCheckInterrupt()方法的阻塞状态中返回(因为被唤醒了)并且
             *  获得了锁,此时thread-B便会执行setHeadAndPropagate方法,head就会发生改变
             * 
             */
            if (h == head)    
                break;
        }
    }
	
	private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head;  // 记录一下旧的头节点
        setHead(node);  // 将当前节点设置为头节点
        /** 
         * 如果propagate > 0 说明锁还没有被别的线程拿到
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }
	
	public final boolean hasQueuedThreads() {
        return head != tail;
    }
	
	public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }
	
	public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }
	
	public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }
	
	private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r); // 区别点 1
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
	
	protected int tryAcquireShared(int arg) {
		throw new UnsupportedOperationException();
	}
	
	public final void acquireShared(int arg) {
		/**
		 * 如果没有获得锁,则放入到等待队列中
		 */
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }
	
	protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }
	
	public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }
	
	public final boolean hasQueuedPredecessors() {
        Node t = tail; 
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
                (s = h.next)  != null &&
                !s.isShared()         &&
                s.thread != null;
    }
	
	/**
	 * 添加一个打印等待队列的方法
	 */
	
	public void printQueue() {
		printQueueNode(tail);
		System.out.println();
	}
	
	public void printQueueNode(Node node) {
		if (node == null) return;
		printQueueNode(node.prev);
		System.out.print(node + "->");
	}
	
	
    private static final Unsafe unsafe;
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
        	
        		Field f = Unsafe.class.getDeclaredField("theUnsafe");
        		f.setAccessible(true);
        		unsafe = (Unsafe)f.get(null);
        		
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
