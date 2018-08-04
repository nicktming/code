package com.exception.jianshu;

public class Test5 {
	public static void main(String[] args) {
		try {
			try {
				throw new MyException();
			} catch (MyException e) {
				System.out.println("deal with this exception");
				throw new MyException("anther exception");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
