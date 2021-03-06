package com.sourcecode.reentrantreadwritelock;

import java.util.HashMap;
import java.util.Map;

public class Cache {
    static Map<String, Object> map = new HashMap<String, Object>();
    static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    static Lock r = rwl.readLock();
    static Lock w = rwl.writeLock();
    // 获取一个key对应的value
    public static final Object get(String key) {
        r.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " gets lock.");
            SleepUnit.sleep(5);
            return map.get(key);
        } finally {
            System.out.println(Thread.currentThread().getName() + " releases lock.");
            r.unlock();
        }
    }
    // 设置key对应的value，并返回旧的value
    public static final Object put(String key, Object value) {
        w.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " gets lock.");
            SleepUnit.sleep(10);
            return map.put(key, value);
        } finally {
            System.out.println(Thread.currentThread().getName() + " releases lock.");
            w.unlock();
        }
    }
    // 清空所有的内容
    public static final void clear() {
        w.lock();
        try {
            map.clear();
        } finally {
            w.unlock();
        }
    }
}
