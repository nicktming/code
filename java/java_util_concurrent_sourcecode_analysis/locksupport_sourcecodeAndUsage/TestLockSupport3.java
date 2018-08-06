package com.sourcecode.locksupports;

public class TestLockSupport3 {

	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread(new Runner1(), "mythread-1");
		thread.start();
		Thread.sleep(2);  //保证thread可以充分运行
		System.out.println("thread blocker:" + LockSupport.getBlocker(thread));
		thread.interrupt();
		Thread.sleep(1);  //保证after park() interrupted status 可以运行
		System.out.println("thread blocker:" + LockSupport.getBlocker(thread));
		System.out.println("main thread end!");
	}
	
	static class Runner1 implements Runnable {
		String str = "i am a blocker instance.";
		@Override
		public void run() {
			System.out.println("before park() interrupted status:" + Thread.currentThread().isInterrupted());
			LockSupport.park(str);
			System.out.println("after park() interrupted status:" + Thread.currentThread().isInterrupted());
		}
	}
}
