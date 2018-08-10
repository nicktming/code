package com.sourcecode.locks;

public class TestReentrantLock {
	static Lock mutexLock = new Mutex();
	static Lock reentrantLock = new ReentrantLock();
	
	public static void main(String[] args) {
		new Runner(mutexLock, "thread-1").start();
		//new Runner(reentrantLock, "thread-1").start();
	}
	
	static class Runner extends Thread {
		Lock lock;
		public Runner(Lock lock, String name) {
			super(name);
			this.lock = lock;
		}
		public void run() {
			lock.lock();
			System.out.println(Thread.currentThread().getName() + " get locks at the first time.");
			lock.lock();
			System.out.println(Thread.currentThread().getName() + " get locks at the second time.");
			lock.unlock();
			System.out.println(Thread.currentThread().getName() + " release locks at the first time.");
			lock.unlock();
			System.out.println(Thread.currentThread().getName() + " release locks at the second time.");
		}
	}
}
