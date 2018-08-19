package com.reference.test;

import java.lang.ref.SoftReference;
public class TestSoftReference {
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("finalize method executed");
	}

	public static void main(String[] args) {
		TestSoftReference tsr = new TestSoftReference();
		System.out.println("tsr instance: " + tsr);
		SoftReference sr = new SoftReference(tsr);
		/**
		 *  此时TestSoftReference的一个对象有两个引用指向它:
		 *  1. 一个强引用tsr
		 *  2. 一个软引用sr
		 */
		System.out.println("before gc: " + sr.get());
		tsr = null;  // 此时只有一个软引用sr指向该对象
		System.gc(); // 启动垃圾回收器
		System.out.println("after  gc: " + sr.get());
	}
}
