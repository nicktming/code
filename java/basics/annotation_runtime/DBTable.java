import java.lang.annotation.*;

@Target(ElementType.TYPE)   //因为要放到类上
@Retention(RetentionPolicy.RUNTIME)
public @interface DBTable {
	String name();
}
