package com.sourcecode.reentrantreadwritelock;

import java.util.concurrent.TimeUnit;

public class SleepUnit {
    public static void sleep(int time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
