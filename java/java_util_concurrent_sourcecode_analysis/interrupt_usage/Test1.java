
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Test1 {

	static CountDownLatch countdown = new CountDownLatch(1);
	//countdown 是为了保证mythread-1充分运行完
	
	public static void main(String[] args) {
		Thread thread = new Thread(new Runner1(), "mythread-1");
		thread.start();
		try {
			TimeUnit.NANOSECONDS.sleep(1);
			thread.interrupt();
			TimeUnit.NANOSECONDS.sleep(1);
			thread.interrupt();
			countdown.await(); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printThreadInfo(Thread.currentThread());
		printThreadInfo(thread);
		System.out.println("main ends!");
	}
	
	static void printThreadInfo(Thread thread) {
		System.out.println(thread.getName() + ", interrupte :" + thread.isInterrupted());
	}
	
	static class Runner1 implements Runnable {
		public void run() {
			System.out.println("Thread-name:" + Thread.currentThread().getName() + ", interrupted:" + Thread.currentThread().isInterrupted());
			int i = 0;
			while(true) {
				if (Thread.interrupted()) {
					System.out.println(Thread.currentThread().getName() + " receives interrupt signal in first loop");
					break;
				}
				System.out.println(Thread.currentThread().getName() + " prints " + (i++) + " in first loop.");
			}
			System.out.println(Thread.currentThread().getName() + " does not stop!");
			printThreadInfo(Thread.currentThread());
			
			
			while (true) {
				if (Thread.currentThread().isInterrupted()) {
					System.out.println(Thread.currentThread().getName() + " receives interrupt signal in second loop");
					break;
				}
				System.out.println(Thread.currentThread().getName() + " prints " + (i++) + " in second loop.");

			}
			printThreadInfo(Thread.currentThread());
			countdown.countDown();
		}
	}

}
