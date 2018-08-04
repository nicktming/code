package com.exception.jianshu;

public class Foo implements AutoCloseable {
	private final String name;
	
	public Foo(String name) {
		this.name = name;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("in close");
	}
	
	
	public static void test() throws Exception {
		try (Foo foo = new Foo("foo")) {
			System.out.println("deal with something");
			throw new RuntimeException();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		try {
			test();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
