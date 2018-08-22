package com.sourcecode.reentrantreadwritelock;

public interface ReadWriteLock {
    Lock readLock();
    Lock writeLock();
}
