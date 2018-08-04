package com.exception.jianshu;

public class Test7 {

	public static void main(String[] args) {
		try {
			testFinallyThrow();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testFinallyThrow() throws MyException {
		try {
			throw new MyException();
		} catch (MyException e) {
			throw new MyException("catch throw a exception");
		} finally {
			throw new MyException("finally throw a exception");
		}
	}
}
