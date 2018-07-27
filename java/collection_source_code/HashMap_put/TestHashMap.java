public class TestHashMap {

	public static void main(String[] args) {
		HashMap<Person, Integer> map = new HashMap<>(3);
		map.put(new Person("tom_1", 12), 12);
		map.put(new Person("tom_2", 0), 0);
		map.put(new Person("tom_3", 4), 4);
		System.out.println("capacity:" + map.table.length);
		printMap(map);
		map.put(new Person("tom_4", 16), 4);
		System.out.println("------------------after insert tom_4---------------------");
		System.out.println("capacity:" + map.table.length);
		printMap(map);
	}
	
	private static void printMap(HashMap<Person, Integer> map) {
		HashMap.Node<Person, Integer>[] table = map.table; 
		for (int i = 0; i < table.length; i++) {
			System.out.print(i + ":");
			HashMap.Node<Person, Integer> e;
			if ((e = table[i]) != null) {
				System.out.print(e);
				HashMap.Node<Person, Integer> p;
				while ((p = e.next) != null) {
					System.out.print("->" + p);
					e = e.next;
				}
			}
			System.out.println();
		}
	}
	
	/*
	private static void printFields(HashMap<Person, Integer> map) {
		System.out.println("modCount:" + map.modCount);
		System.out.println("size:" + map.size);
		System.out.println("threshold:" + map.threshold);
	}
	*/
	
}
