package com.exception.jianshu;

public class Test3 {
	public static void main(String[] args) {
		try {
			try {
				throw new MyException("in my firstMyException");
			} finally {
				System.out.println("in finally");
				throw new MyAnotherException();
			}
		} catch (MyAnotherException e) {
			e.printStackTrace();
		}
	}
}
