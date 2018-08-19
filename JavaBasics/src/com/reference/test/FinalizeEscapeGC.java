package com.reference.test;

public class FinalizeEscapeGC {
	public static FinalizeEscapeGC SAVE_HOOK = null;
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("finalize method executed");
		FinalizeEscapeGC.SAVE_HOOK = this;
	}
	public static void helpGC() throws Throwable {
		SAVE_HOOK = null;
		System.gc();
		Thread.sleep(500);
		if (SAVE_HOOK != null) {
			System.out.println("yes, i am still alive.");
		} else {
			System.out.println("no, i am dead.");
		}
	}
	
	public static void main(String[] args) throws Throwable {
		SAVE_HOOK = new FinalizeEscapeGC();
		helpGC(); // 第一次执行了finalize自救
		helpGC(); // finalize执行过了一次便不再执行了
		Object obj = new Object();
	}
}
