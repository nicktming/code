import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLBoolean {
	String name() default "";
	Constraints constraints() default @Constraints;
}
