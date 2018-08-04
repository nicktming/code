package com.exception.jianshu;

public class MyThirdException extends Exception {
	/*
	public synchronized Throwable fillInStackTrace() {
		System.out.println("in fillInStackTrace()");
		return this;
	}
	*/
	
	public static void g() throws MyThirdException {
		throw new MyThirdException();
	}
	
	public static void main(String[] args) {
		try {
			g();
		} catch (MyThirdException e) {
			e.printStackTrace(System.out);
		}
	}
}
