package com.reference.test;

import java.lang.ref.WeakReference;
public class TestWeakReference {

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("finalize method executed");
	}

	public static void main(String[] args) {
		TestWeakReference twr = new TestWeakReference();
		WeakReference wr = new WeakReference(twr);
		/**
		 *  此时TestWeakReference的一个对象有两个引用指向它:
		 *  1. 一个强引用twr
		 *  2. 一个弱引用sr
		 */
		System.out.println("before gc: " + wr.get());
		twr = null;  //去掉强引用twr
		System.gc();
		System.out.println("after  gc: " + wr.get());
	}
}
