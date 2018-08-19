package com.reference.test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class TestReferenceQueue {

	public static void main(String[] args) {
		ReferenceQueue rq = new ReferenceQueue();
		WeakReference wr = new WeakReference(new TestReferenceQueue(), rq); 
		System.out.println("弱引用对应的对象:" + wr.get() + ", 弱引用本身:" + wr);
		System.out.println("队列中对象:" + rq.poll());
		/**
		 * TestReferenceQueue中的对象只有一个引用 就是wr弱引用
		 * 因此直接调用gc就可以
		 */
		System.gc();
		System.out.println("弱引用对应的对象:" + wr.get() + ", 弱引用本身:" + wr);
		System.out.println("队列中对象:" + rq.poll());
	}

}
