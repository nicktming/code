import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Test2 {
	
	public static void main(String[] args) {
		Object obj = new Object();
		Thread thread = new Thread(new Runner2(obj), "mythread-1");
		thread.start();
		try {
			TimeUnit.MILLISECONDS.sleep(1);
			thread.interrupt();
			TimeUnit.MILLISECONDS.sleep(1);
			thread.interrupt();
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		printThreadInfo("in main end ", thread);
	}
	
	static void printThreadInfo(String info, Thread thread) {
		System.out.println(info + thread.getName() + ", interrupte :" + thread.isInterrupted());
	}
	
	static class Runner2 implements Runnable {
		Object obj;
		public Runner2(Object obj) {
			this.obj = obj;
		}
		public void run() {
			synchronized(obj) {
				try {
					System.out.println(Thread.currentThread().getName() + " before waits");
					obj.wait();
					System.out.println(Thread.currentThread().getName() + " after waits");
				} catch (InterruptedException e) {
					e.printStackTrace();
					printThreadInfo("in wait catch ", Thread.currentThread());
				} 
			}
			try {
				System.out.println(Thread.currentThread().getName() + " before sleep");
				Thread.sleep(1000);
				System.out.println(Thread.currentThread().getName() + " after sleep");
			} catch (InterruptedException e) {
				e.printStackTrace();
				printThreadInfo("in sleep catch ", Thread.currentThread());
			//	Thread.currentThread().interrupt();
			} 
			
			System.out.println("end!");
		}
	}
}
