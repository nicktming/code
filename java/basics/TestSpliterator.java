import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class TestSpliterator {
	
	
	public static void main(String[] args) {
		test_forEachRemaining();
		System.out.println("---------------------");
		test_tryAdvance();
	}
	
	public static void test_tryAdvance() {
		ArrayList<Integer> al = new ArrayList<>();
		for (int i = 0; i <= 10; i++) al.add(i);
		ArrayListSpliterator als_1 = new ArrayListSpliterator(al, 0, -1);
		als_1.tryAdvance(new Consumer<Integer>(){
			@Override
			public void accept(Integer t) {
				System.out.print(t + " ");
			}
		});
		System.out.println("\nals_1:" + als_1);
		System.out.println("left size:" + als_1.estimateSize());
	}
	
	public static void test_forEachRemaining() {
		ArrayList<Integer> al = new ArrayList<>();
		for (int i = 0; i <= 10; i++) al.add(i);
		ArrayListSpliterator als_1 = new ArrayListSpliterator(al, 0, -1);
		als_1.forEachRemaining(new Consumer<Integer>(){
			@Override
			public void accept(Integer t) {
				System.out.print(t + " ");
			}
		});
		System.out.println("\nals_1:" + als_1);
		System.out.println("left size:" + als_1.estimateSize());
	}
	
	public static void test_trySplit() {
		ArrayList<Integer> al = new ArrayList<>();
		for (int i = 0; i <= 10; i++) al.add(i);
		ArrayListSpliterator als_1 = new ArrayListSpliterator(al, 0, -1);
		System.out.println("als_1:" + als_1);    // [0,11]
		
System.out.println("---------split-----------");
		ArrayListSpliterator als_2 = als_1.trySplit();
		System.out.println("als_1:" + als_1);    // [5,11]
		System.out.println("als_2:" + als_2);    // [0,5]
		
		// [0,11](als_1) ---> [0,5](als_2) + [5,11](als_1)
		
System.out.println("---------split-----------");
		ArrayListSpliterator als_3 = als_1.trySplit();
		ArrayListSpliterator als_4 = als_2.trySplit();
		System.out.println("als_1:" + als_1);
		System.out.println("als_2:" + als_2);
		System.out.println("als_3:" + als_3);
		System.out.println("als_4:" + als_4);
		
		/**
		 * [0,5](als_2)  --> [0,2](als_4)  + [2,5](als_2)
		 * [5,11](als_1) --> [8,11](als_1) + [5,8](als_3)
		 */
		
System.out.println("---------test the address---------");
		System.out.println("(als_1.list == als_2.list) = " + (als_1.list == als_2.list));
		System.out.println("(als_2.list == als_3.list) = " + (als_2.list == als_3.list));
		System.out.println("(als_3.list == als_4.list) = " + (als_3.list == als_4.list));
	}
	
	
	static final class ArrayListSpliterator<E> implements Spliterator<E> {
		
		//用于存放实体变量的list
        private final ArrayList<E> list;
        //遍历的当前位置
        private int index; 
        //结束位置(不包括) 意思是当前可用的元素是[index, fence) = [index, fence-1] 
        private int fence; // -1 until used; then one past last index

        // 构造方法
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence) {
            this.list = list; 
            this.index = origin;
            this.fence = fence;
        }
        
        //第一次使用的时候初始化fence 返回结束位置
        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    hi = fence = lst.size();
                }
            }
            return hi;
        }
        
        /**
         * 根据当前的Spliterator拆分出一个新的Spliterator
         * 相当于二分,
         * Note:共享同一个list,改变的只是下标
         */
        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                new ArrayListSpliterator<E>(list, lo, index = mid);
        }
        
        //单次遍历  下标index只加1
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.get(i);
                action.accept(e);
                return true;
            }
            return false;
        }
        
        //整体遍历 
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.toArray()) != null) {
                if ((hi = fence) < 0) {
                    hi = lst.size();
                }
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                }
            }
        }
        
        //剩下还有多少元素
        public long estimateSize() {
            return (long) (getFence() - index);
        }
        
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        
        public String toString() {
        		return "[" + this.index + "," + getFence() + "]";
        }
    }
}
