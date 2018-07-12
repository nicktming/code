import java.lang.annotation.*;
import java.lang.reflect.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Testable {
	//String name();
	String name() default "no name";
	String value() default "no value";
	//String value();
}

class TestValue {
	@Testable("value")
	public void test_1() {}
}

class Parse {
	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		Method m = TestValue.class.getMethod("test_1");
		Testable testable = m.getAnnotation(Testable.class);
		System.out.println("name=" + testable.name() + ", value=" + testable.value());
	}
}

