import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLString {
	String name() default "";
	int value() default 0;
	Constraints constraints() default @Constraints;
}
