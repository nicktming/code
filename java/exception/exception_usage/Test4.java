package com.exception.jianshu;

public class Test4 {
	static void f() {
		try {
			throw new MyException();
		} catch (MyException e) {
			for (StackTraceElement ste : e.getStackTrace()) {
				System.out.println(ste.getMethodName() + " " + ste.getLineNumber());
			}
		}
	}
	static void g() {f();}
	public static void main(String[] args) {
		g();
	}
}
