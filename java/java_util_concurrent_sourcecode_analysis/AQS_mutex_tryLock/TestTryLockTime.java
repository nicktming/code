package com.sourcecode.locks;

import java.util.concurrent.TimeUnit;

public class TestTryLockTime {
	public static void main(String[] args) {
		Mutex m = new Mutex();
		for (int i = 0; i < 5; i++) {
			new Thread(new Runner(m), "thread-" + i).start();;
		}
		try {
			TimeUnit.SECONDS.sleep(3); // 为了让每个thread充分运行
			m.printWaitingNode();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	static class Runner implements Runnable {
		Mutex m;
		public Runner(Mutex m) {
			this.m = m;
		}
		@Override
		public void run() {
			boolean getLock = false;
			try {
				getLock = m.tryLock(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (getLock && !Thread.currentThread().isInterrupted()) {
				System.out.println(Thread.currentThread().getName() + " get lock and runs");
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
				m.unlock();
			} else {
				System.out.println(Thread.currentThread().getName() + " does not get lock and runs");
			}
		}
	}
}
