package com.sourcecode.locksupports;

public class TestLockSupport2 {
	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread(new Runner1(), "mythread-1");
		thread.start();
		Thread.sleep(1);  //保证thread可以充分运行
		thread.interrupt();
		Thread.sleep(1);  //保证after park() interrupted status 可以运行
		System.out.println("main thread end!");
	}
	
	static class Runner1 implements Runnable {
		@Override
		public void run() {
			System.out.println("before park() interrupted status:" + Thread.currentThread().isInterrupted());
			LockSupport.park();
			System.out.println("after park() interrupted status:" + Thread.currentThread().isInterrupted());
		}
	}
}
