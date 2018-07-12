import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestCaseParse {
	public static void main(String[] args) {
		testCaseParse(Cases.class);
		Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6));
		countMissingTestCase(Cases.class, set);
	}
	
	public static void testCaseParse(Class<?> clazz) {
		// 拿到所有的方法(包括private)
		Method[] methods = clazz.getDeclaredMethods(); 
		for (Method m : methods) {
			//两种方式都可以拿到m方法上的TestCase注解实例
			//TestCase testCase1 = (TestCase)m.getAnnotations()[0];
			//TestCase testCase2 = m.getAnnotation(TestCase.class);
			TestCase testCase = m.getAnnotation(TestCase.class);
			System.out.println("id=" + testCase.id() + ", name=" + testCase.name());
		}
	}
	
	public static void countMissingTestCase(Class<?> clazz, Set<Integer> set) {
		Method[] methods = clazz.getDeclaredMethods(); // 拿到所有的方法(包括private)
		for (Method m : methods) {
			TestCase testCase = m.getAnnotation(TestCase.class);
			int id = testCase.id();
			if (set.contains(id)) set.remove(id);
		}
		System.out.println("missing testcase : ");
		for (int id : set) System.out.println("id=" + id);
	}
}
