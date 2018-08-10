package com.sourcecode.locks;

import java.util.concurrent.CountDownLatch;
public class TestFairAndNonFairLock {
	static ReentrantLock nonfair = new ReentrantLock(false);
	static ReentrantLock fair = new ReentrantLock(true);
	static CountDownLatch start = new CountDownLatch(1);
	// start 是为了保证5个线程同时运行 后续有专门博客会分析CountDownLatch
	public static void main(String[] args) {
		//test(fair);
		test(nonfair);
	}
	
	public static void test (ReentrantLock lock) {
		for (int i = 0; i < 5; i ++) {
			new Thread(new Runner2(lock), i+"").start();
		}
		start.countDown();
	}
	
	static class Runner2 implements Runnable {
		
		ReentrantLock lock;
		public Runner2(ReentrantLock lock) {
			this.lock = lock;
		}
		
		public void run() {
			try {
				start.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lock.lock();
			System.out.print("lock by " + Thread.currentThread().getName() + " wait by [");
			String str = "";
			for (Thread t : lock.getQueuedThreads()) {
				str = t.getName() + "," + str;
			}
			System.out.println(str + "]");
			lock.unlock();
			
			lock.lock();
			System.out.print("lock by " + Thread.currentThread().getName() + " wait by [");
			str = "";
			for (Thread t : lock.getQueuedThreads()) {
				str = t.getName() + "," + str;
			}
			System.out.println(str + "]");
			lock.unlock();
		}
	}
}
