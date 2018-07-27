import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class CreateSQL {

	public static void main(String[] args) {
		System.out.println(createSQL(User.class));
	}
	
	public static String createSQL(Class<?> clazz) {
		StringBuffer sbuffer = new StringBuffer();
		DBTable table = clazz.getAnnotation(DBTable.class);
		if (table == null) return null;
		sbuffer.append("create table " + table.name() + "(\n");
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			String columnName = f.getName();
			Annotation anno = f.getAnnotations()[0];
			if (anno instanceof SQLString) {
				sbuffer.append("   " + sqlStr((SQLString)anno, columnName) + "\n");
			} else if (anno instanceof SQLInteger) {
				sbuffer.append("   " + sqlInt((SQLInteger)anno, columnName) + "\n");
			} else if (anno instanceof SQLBoolean) {
				sbuffer.append("   " + sqlBool((SQLBoolean)anno, columnName) + "\n");
			} else if (anno instanceof SQLCharacter) {
				sbuffer.append("   " + sqlCharacter((SQLCharacter)anno, columnName) + "\n");
			}
		}
		return sbuffer.append(");").toString();
	}
	
	public static String sqlCharacter(SQLCharacter anno, String columnName) {
		if (anno.name().length() > 0) columnName = anno.name();
		return columnName + " Character(" + anno.value() + ")" + getConstraints(anno.constraints());
	}
	
	public static String sqlBool(SQLBoolean anno, String columnName) {
		if (anno.name().length() > 0) columnName = anno.name();
		return columnName + " Boolean" + getConstraints(anno.constraints());
	}
	
	public static String sqlInt(SQLInteger anno, String columnName) {
		if (anno.name().length() > 0) columnName = anno.name();
		return columnName + " Integer" + getConstraints(anno.constraints());
	}
	
	public static String sqlStr(SQLString anno, String columnName) {
		if (anno.name().length() > 0) columnName = anno.name();
		return columnName + " VARCHAR(" + anno.value() + ")" + getConstraints(anno.constraints());
	}
	
	public static String getConstraints(Constraints cons) {
		String str = "";
		if (cons.primaryKey()) str += " PRIMARYKEY";
		if (!cons.allowNull()) str += " NOT NULL";
		if (cons.unique())     str += " UNIQUE";
		return str;
	}

}
