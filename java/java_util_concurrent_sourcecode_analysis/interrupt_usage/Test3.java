import java.util.concurrent.TimeUnit;

public class Test3 {

	public static void main(String[] args) {
		
	}
	
	public static void mySleep(long timeout) throws InterruptedException {
		System.out.println("i want to sleep.");
		TimeUnit.MILLISECONDS.sleep(timeout);
		System.out.println("wake up");
	}
	
	public static void mySleep1(long timeout) throws InterruptedException {
		System.out.println("i want to sleep.");
		try {
			TimeUnit.MILLISECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			//clear somthing
			throw e;
		}
		System.out.println("wake up");
	}
	
	static class Runner implements Runnable {
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace(); 
				Thread.currentThread().interrupt();
			}
		}
	}

}
