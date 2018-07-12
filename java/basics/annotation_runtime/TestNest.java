import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface Nest {
	boolean display() default true;
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface OutNest {
	String name() default "no name";
	Nest nest() default @Nest;
}

public class TestNest {
	public static void main(String[] args) throws NoSuchMethodException, SecurityException{
		OutNest outNest = TestNest.class.getMethod("test").getAnnotation(OutNest.class);
		System.out.println("name=" + outNest.name() + ", nest=" + outNest.nest());
		System.out.println("display=" + outNest.nest().display());
	} 
	
	@OutNest(name="test", nest=@Nest(display=false))
	public static void test() {}
}
