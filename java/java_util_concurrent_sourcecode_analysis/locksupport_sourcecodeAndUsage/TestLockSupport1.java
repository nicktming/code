package com.sourcecode.locksupports;

public class TestLockSupport1 {
	public static void main(String[] args) {
		//test_1();
		//test_2();
		//test_3();
		//test_4();
	}
	
	public static void test_1() {
		//默认的时候当前线程的_counter = 0
		LockSupport.unpark(Thread.currentThread()); //_counter = 1
		LockSupport.park(); //_counter = 0
		System.out.println("i can execute."); // 可以执行
	}
	
	public static void test_2() {
		//默认的时候当前线程的_counter = 0
		LockSupport.park(); //阻塞
		System.out.println("i cannot execute."); //不能执行
	}
	
	public static void test_3() {
		LockSupport.parkNanos(10);
		System.out.println("i can execute after 10ns"); //此时的_counter等于0
		LockSupport.park();
		System.out.println("i cannnot execute");
	}
	
	public static void test_4() {
		long start = System.currentTimeMillis();
		System.out.println("start:" + start);
		LockSupport.parkUntil(start + 10000);
		System.out.println("end:" + System.currentTimeMillis());
		System.out.println("i can execute after 10s"); //此时的_counter等于0
		LockSupport.park();
		System.out.println("i cannnot execute");
	}
}
