package com.sourcecode.locks;

import java.util.concurrent.TimeUnit;

public class Test {
	public static void main(String[] args) {
		Mutex m = new Mutex();
		for (int i = 0; i < 5; i++) {
			new Thread(new Runner(m), "thread-" + i).start();;
		}
		for (int i = 0; i < 5; i++) {
			m.printWaitingNode();
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	static class Runner implements Runnable {
		Mutex m;
		public Runner(Mutex m) {
			this.m = m;
		}
		@Override
		public void run() {
			m.lock();
			System.out.println(Thread.currentThread().getName() + " runs");
			try {
				TimeUnit.SECONDS.sleep(6);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m.unlock();
		}
	}
}
