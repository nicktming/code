package com.sourcecode.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class TwinsLock implements Lock {
	private final Sync sync = new Sync(2);
	private static final class Sync extends AbstractQueuedSynchronizer {
		public Sync (int count) {
			if (count <= 0) {
				throw new IllegalArgumentException("count must larger than zero.");
			}
			super.setState(count);
		}
		public int tryAcquireShared(int reduceCount) {
			for (;;) {
				int current = super.getState();
				int newCount = current - reduceCount;
				if (newCount < 0 || super.compareAndSetState(current, newCount)) {
					return newCount;
				}
			}
		}
		public boolean tryReleaseShared(int returnCount) {
			for (;;) {
				int current = super.getState();
				int newCount = current + returnCount;
				if (super.compareAndSetState(current, newCount)) {
					return true;
				}
			}
		}
	}
	public void printWaitingNode() {
		sync.printQueue();
	}
	@Override
	public void lock() {
		sync.acquireShared(1);
	}
	@Override
	public void unlock() {
		sync.releaseShared(1);
	}
	@Override
	public void lockInterruptibly() throws InterruptedException {
		// TODO Auto-generated method stub
	}
	@Override
	public boolean tryLock() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Condition newCondition() {
		// TODO Auto-generated method stub
		return null;
	}
}
