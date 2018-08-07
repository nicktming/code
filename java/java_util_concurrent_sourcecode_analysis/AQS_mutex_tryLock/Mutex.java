package com.sourcecode.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class Mutex implements Lock {
	
	private final Sync sync = new Sync();
	
	private static class Sync extends AbstractQueuedSynchronizer {
		protected boolean isHeldExclusively() {
			return getState() == 1;
		}
		public boolean tryAcquire(int acquires) {
			if (super.compareAndSetState(0, 1)) {
				super.setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;
		}
		public boolean tryRelease(int releases) {
			if (super.getState() == 0) 
				throw new IllegalMonitorStateException();
			super.setExclusiveOwnerThread(null);
			super.setState(0);
			return true;
		}
	}
	
	public void printWaitingNode() {
		sync.printQueue();
	}

	@Override
	public void lock() {
		sync.acquire(1);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock() {
		// TODO Auto-generated method stub
		return sync.tryAcquire(1);
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return sync.tryAcquireNanos(1, unit.toNanos(time));
	}

	@Override
	public void unlock() {
		// TODO Auto-generated method stub
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		// TODO Auto-generated method stub
		return null;
	}
}
