package com.sourcecode.concurrencytools_Semaphore;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SemaphoreTest {
    static Random random = new Random();
    static class ParkingLot {
        Semaphore semaphore;
        ParkingLot(int size) {
            semaphore = new Semaphore(size);
        }
        public void park() {
            try {
                semaphore.acquire();
                int waitTime = random.nextInt(10);
                System.out.println(Thread.currentThread().getName() + " parks, it takes " + waitTime + " seconds.");
                TimeUnit.SECONDS.sleep(waitTime);
                System.out.println(Thread.currentThread().getName() + " leaves.");
                semaphore.release();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Car extends Thread {
        ParkingLot parkingLot;
        public Car(ParkingLot parkingLot) {
            this.parkingLot = parkingLot;
        }
        public void run() {
            this.parkingLot.park();
        }
    }

    public static void main(String[] args){
        ParkingLot parking = new ParkingLot(3);
        for(int i = 0 ; i < 6 ; i++){
            new Car(parking).start();
        }
    }
}
