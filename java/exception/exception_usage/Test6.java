package com.exception.jianshu;

public class Test6 {
	public static void main(String[] args) {
		try {
			try {
				throw new MyException();
			} catch (MyException e) {
				System.out.println("deal with this exception");
				throw new MyCauseException(e);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class MyCauseException extends Exception {
	public MyCauseException() {
		
	}
	
	public MyCauseException(String msg) {
		super(msg);
	}
	
	public MyCauseException(Throwable cause) {
		super(cause);
	}
}
