package com.exception.jianshu;

public class Test2 {
	public static int f(int i) {
		try {
			if (i == 1) return 1;
			if (i == 2) throw new MyException();
			return 3;
		} catch (MyException e) {
			System.out.println("in MyException");
			return -2;
		} finally {
			System.out.println("in finally");
			if (i == 0 || i == 2) return 0;
		}
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 3; i++) {
			System.out.println("return:" + f(i));
			System.out.println("----------\n");
		}
	}
}
