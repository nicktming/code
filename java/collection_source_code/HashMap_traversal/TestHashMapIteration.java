import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;

public class TestHashMapIteration {

	public static void main(String[] args) {
		test_spliterator();
	}
	
	public static void test_spliterator() {
		HashMap<String, Integer> map = new HashMap<>();
		String str = "ABCDEFG";
		for (int i = 0; i < str.length(); i++) {
			map.put(str.charAt(i) + "", i);
		}
		System.out.println("capacity:" + map.table.length);
		Spliterator<Map.Entry<String,Integer>> ster_1 = map.entrySet().spliterator();
		System.out.println("ster_1:" + ster_1);
		Spliterator<Map.Entry<String,Integer>> ster_2 = ster_1.trySplit();
		System.out.println("----------split------------");
		System.out.println("ster_1:" + ster_1);
		System.out.println("ster_2:" + ster_2);
		Spliterator<Map.Entry<String,Integer>> ster_3 = ster_1.trySplit();
		Spliterator<Map.Entry<String,Integer>> ster_4 = ster_2.trySplit();
		System.out.println("----------split------------");
		System.out.println("ster_1:" + ster_1);
		System.out.println("ster_2:" + ster_2);
		System.out.println("ster_3:" + ster_3);
		System.out.println("ster_4:" + ster_4);
		
		System.out.println("------ster1-------");
		printSpliterator(map, ster_1);
		System.out.println("------ster2-------");
		printSpliterator(map, ster_2);
		System.out.println("------ster3-------");
		printSpliterator(map, ster_3);
		System.out.println("------ster4-------");
		printSpliterator(map, ster_4);
	}
	
	private static void printSpliterator(HashMap<String, Integer> map, Spliterator<Map.Entry<String,Integer>> ster) {
		ster.forEachRemaining(new Consumer<Map.Entry<String,Integer>>(){

			@Override
			public void accept(Entry<String, Integer> t) {
				System.out.print("[" + t.getKey() + "->" + t.getValue() + "] ");
				//map.remove("A");   //java.util.ConcurrentModificationException
				//map.put("A", -1);    //更新节点 不影响
			}
			
		});
		System.out.println();
	}
	
	public static void test_iterator() {
		HashMap<String, Integer> map = new HashMap<>();
		String str = "ABCDEFG";
		for (int i = 0; i < str.length(); i++) {
			map.put(str.charAt(i) + "", i);
		}
		System.out.println("modCount->" + map.modCount);
		// foreach是调用iterator()方法返回迭代器 用迭代器来遍历
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			System.out.print("[" + entry.getKey() + "->" + entry.getValue() + "] ");
		}
		System.out.println("\nmodCount->" + map.modCount);
		System.out.println("-----------------------------------");
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iter.next();
			System.out.print("[" + entry.getKey() + "->" + entry.getValue() + "] ");
			//map.remove(entry.getKey()); // 会产生java.util.ConcurrentModificationException
			//map.put("X", -1);  //会产生java.util.ConcurrentModificationException
			//map.put("A", 2);  //不会有影响,因为是更新
			//iter.remove(); //没有影响  
		}
		System.out.println("\nmodCount->" + map.modCount);
		System.out.println("size:" + map.size);
		
		for (String k : map.keySet()) {
			System.out.print("[" + k + "->" + map.get(k) + "] ");
		}
	}
}
