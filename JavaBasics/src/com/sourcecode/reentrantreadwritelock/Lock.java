package com.sourcecode.reentrantreadwritelock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public interface Lock {
	// 获取锁
	void lock();
	// 获取锁 响应中断
	void lockInterruptibly() throws InterruptedException;
	// 获取锁 如果锁是available立即返回true, 如果锁不存在就立即返回false
	boolean tryLock();
	// 在上面的基础上加一个时间限制
	boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
	// 释放锁
	void unlock();
	// 后续博客会讨论
	Condition newCondition();
	
}
