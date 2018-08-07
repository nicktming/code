package com.sourcecode.locks;

import java.util.concurrent.TimeUnit;
import com.sourcecode.locks.Test.Runner;

public class TestLockInterruptedException {
	public static void main(String[] args) {
		Mutex m = new Mutex();
		Thread thread_1 = new Thread(new Runner(m), "thread-1");
		Thread thread_2 = new Thread(new Runner(m), "thread-2");
		thread_1.start();
		try {
			TimeUnit.SECONDS.sleep(1); //让thread-1获得锁
			thread_2.start();
			TimeUnit.SECONDS.sleep(1); //让thread-2充分进入到等待队列中
			m.printWaitingNode();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		thread_2.interrupt();
	}
	
	static class Runner implements Runnable {
		Mutex m;
		public Runner(Mutex m) {
			this.m = m;
		}
		@Override
		public void run() {
			boolean getLock = true;
			try {
				m.lockInterruptibly();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(Thread.currentThread().getName() + " intrrupted status:" + Thread.currentThread().isInterrupted());
				Thread.currentThread().interrupt(); //报告一下中断状态  因为抛出异常前中断状态被清空了
				getLock = false;
			}
			System.out.println(Thread.currentThread().getName() + " runs, getLock: " + getLock);
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(getLock) m.unlock();
		}
	}
}
