package com.exception.jianshu;

public class Test8 {
	public static void main(String[] args) {
		g();
	}
	
	public static void testRuntime() {
		throw new RuntimeException();
	}
	
	static void g() {
		testRuntime();
	}
}
