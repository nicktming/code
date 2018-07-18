import java.util.ArrayList;
import java.util.Arrays;
public class Test1 {
	public static void main(String[] args) {
		System.out.println("in Test1");
		ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1,2,3,4,5));
		for (int i : list) System.out.println("element:" + i);
		System.out.println("Bootclasspath:" + System.getProperty("sun.boot.class.path"));
		System.out.println("user classes:" + System.getProperty("java.class.path"));
	}
}