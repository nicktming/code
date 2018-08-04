package com.exception.jianshu;

public class Test1 {
	public static void main(String[] args) {
		try {
			for (int i = 0; i < 5; i++) {
				if (i == 2) throw new MyException("i equals 1");
			}
		} catch (MyException e) {
			System.out.println("in MyException");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("in Exception");
			e.printStackTrace();
		} finally {
			System.out.println("in finally");
		}
	}
}
