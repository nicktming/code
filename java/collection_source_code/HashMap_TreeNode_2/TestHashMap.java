import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TestHashMap {

	public static void main(String[] args) {
		test_redblacktree_put();
		//test_system_hashcode();
	}
	
	static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                        ((p = (ParameterizedType)t).getRawType() ==
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }
	
	public static void test_system_hashcode () {
		Object obj = new Object();
		Person p1 = new Person();
		Person p2 = new Person();
		System.out.println(p1.hashCode());
		System.out.println(p2.hashCode());
		
		System.out.println("p1->" + System.identityHashCode(p1));
		System.out.println("p2->" + System.identityHashCode(p2));
	}
	
	public static void test_redblacktree_put() {
		HashMap<Person, Integer> map = new HashMap<>(64);
		char[] strs = "SEARCHXMPL".toCharArray();
		for (int i = 0; i < strs.length; i++) {
			System.out.println("insert into " + strs[i]);
			map.put(new Person(strs[i] + "", (strs[i] - '0') * 64),  i);
			printMap(map);
		}
		map.put(new Person("Z", ('M' - '0') * 64), -1);
		printMap(map);
	}
	
	public static void test_put() {
		HashMap<Person, Integer> map = new HashMap<>(3);
		map.put(new Person("tom_1", 12), 12);
		map.put(new Person("tom_2", 0), 0);
		map.put(new Person("tom_3", 4), 4);
		System.out.println("capacity:" + map.table.length);
		//printMap(map);
		map.put(new Person("tom_4", 16), 16);
		//System.out.println("------------------after insert tom_4---------------------");
		System.out.println("capacity:" + map.table.length);
		printMap(map);
		System.out.println("---------------------------------------------------------");	
		
		boolean f = map.remove(new Person("tom_4", 4), 4); //没有删除成功 因为必须key和value都要对应上才可以删除
		System.out.println("delete : " + f);
		printMap(map);
		System.out.println("---------------------------------------------------------");		
		map.remove(new Person("tom_4", 16));
		map.remove(new Person("tom_3", 4));
		map.remove(new Person("tom_2", 0));
		map.remove(new Person("tom_1", 12));
		printMap(map);
	}
	
	private static void printMap(HashMap<Person, Integer> map) {
		HashMap.Node<Person, Integer>[] table = map.table; 
		for (int i = 0; i < table.length; i++) {
			
			HashMap.Node<Person, Integer> e;
			if ((e = table[i]) != null) {
				System.out.print(i + ":");
				System.out.print(e);
				HashMap.Node<Person, Integer> p;
				while ((p = e.next) != null) {
					System.out.print("->" + p);
					e = e.next;
				}
				System.out.println();
			}
			
		}
	}
	
}
