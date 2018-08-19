package com.reference.test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public class TestPhantomReference {
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("finalize method executed");
	}

	public static void main(String[] args) {
		ReferenceQueue rq = new ReferenceQueue();
		TestWeakReference twr = new TestWeakReference();
		PhantomReference pr = new PhantomReference(twr, rq);
		System.out.println("before gc: " + pr.get() + ", " + rq.poll());
		twr = null;  //去掉强引用twr
		System.gc();
		System.out.println("after  gc: " + pr.get() + "," + rq.poll());
	}
}
