package com.concurrentlinkedqueue;

public class TestBreakContinue {
    public static void main(String[] args) {
        //test();
        //testBreak1();
        //testBreak2();
        //testBreak3();
        //testContinue1();
        //testContinue2();
        testContinue3();
    }

    public static void test() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.println("i=" + i + ",j=" + j);
            }
        }
    }

    public static void testBreak1() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    break;
                }
                System.out.println("i=" + i + ",j=" + j);
            }
        }
    }

    public static void testBreak2() {
        for (int i = 0; i < 2; i++) {
            inLabel:
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    break inLabel;
                }
                System.out.println("i=" + i + ",j=" + j);
            }
        }
    }

    public static void testBreak3() {
        outLabel:
        for (int i = 0; i < 2; i++) {
            inLabel:
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    break outLabel;
                }
                System.out.println("i=" + i + ",j=" + j);
            }
        }
    }

    public static void testContinue1() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    continue;
                }
                System.out.println("i=" + i + ",j=" + j);
            }
        }
    }

    public static void testContinue2() {
        for (int i = 0; i < 2; i++) {
            inLabel:
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    continue inLabel;
                }
                System.out.println("i=" + i + ",j=" + j);
            }
        }
    }

    public static void testContinue3() {
        outLabel:
        for (int i = 0; i < 2; i++) {
            inLabel:
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    continue outLabel;
                }
                System.out.println("i=" + i + ",j=" + j);
            }
        }
    }

}
