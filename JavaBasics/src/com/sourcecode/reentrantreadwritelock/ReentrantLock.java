package com.sourcecode.reentrantreadwritelock;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class ReentrantLock implements Lock, java.io.Serializable {
	private static final long serialVersionUID = 7373984872572414699L;
	
	abstract static class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = -5179523762034025860L;
		// 留给子类实现
		abstract void lock();
		
		final boolean nonfairTryAcquire(int acquires) {
			final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {  // 第一次获得锁
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {  // 重入该锁 只是累加状态
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
		}
		
		protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException(); // 属于运行时异常
            boolean free = false;
            if (c == 0) {   // 判断该线程是否完全退出
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);  // 设置新的状态
            return free;
        }
		
		// 判断当前线程是否持有该锁
		protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
		// condition相关博客会分析
        final ConditionObject newCondition() {
            return new ConditionObject();
        }
        // 获得持有该锁的线程
        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }
        // 获得持有该锁的个数 其实就是重入的次数
        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }
        // 判断锁有没有被占用 true表示被占用 false表示没有被占用
        final boolean isLocked() {
            return getState() != 0;
        }
        // 序列化的部分
        private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
                s.defaultReadObject();
                setState(0); // reset to unlocked state
        }
	}
	
	static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;
        
        // 实现父类的方法
        final void lock() {
            /**
             * 如果获取锁成功,则设置当前线程
             * 否则去尝试获取锁
             */
            if (compareAndSetState(0, 1)) 
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }
        	
        // 重写父类的父类AbstractQueuedSynchronizer的tryAcquire方法
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }
	
	static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }
        
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) { // 检查等待队列前面是不是有线程还没有获得锁
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
	
	private final Sync sync;
	
	public ReentrantLock() {
		sync = new NonfairSync();
	}
	
	public ReentrantLock(boolean fair) {
		sync = fair ? new FairSync() : new NonfairSync();
	}
	

	@Override
	public void lock() {
		sync.lock();
	}
	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}
	@Override
	public boolean tryLock() {
		return sync.nonfairTryAcquire(1);
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(time));
	}

	@Override
	public void unlock() {
		sync.release(1);
	}


	public Condition newCondition() {
		return sync.newCondition();
	}
	
	// 一些监控信息的方法
	
	// 获取锁被重入了多少次
	public int getHoldCount() {
		return sync.getHoldCount();
	}
	
	// 判断锁是不是被当前线程持有
	public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }
	
	// 判断锁有没有被任何一个线程占有
	public boolean isLocked() {
	        return sync.isLocked();
	}
	// 判断该锁是不是公平锁
	public final boolean isFair() {
        return sync instanceof FairSync;
    }
	// 返回占有锁的那个线程
	protected Thread getOwner() {
        return sync.getOwner();
    }
	// 返回等待队列中是否还有节点 
	public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }
	// thread是否在等待队列中
	public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }
	// 返回等待队列中的长度
	public final int getQueueLength() {
        return sync.getQueueLength();
    }
	// 返回等待队列中的所有线程
	protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }
	
	public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?
                                   "[Unlocked]" :
                                   "[Locked by thread " + o.getName() + "]");
    }
	
	// 关于condition的暂时还没有添加进来,后续会有专门的博客分析
	// Condition
}
