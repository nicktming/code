package com.sourcecode.threadlocal;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocal<T> {
	
	public static void main(String[] args) {
        test_2();
	}

	public static void test_3() {
        ThreadLocal<Integer> [] tls = new ThreadLocal[100000000];
        for (int i = 0; i < tls.length; i++) {
            tls[i] = new ThreadLocal<Integer>();
        }
        ThreadLocalMap map = new ThreadLocalMap(tls[0], 0);
        for (int i = 1; i < 9; i++) {
            map.set(tls[i], i);
        }
        for (int i = 0; i < tls.length; i++) {
            tls[i] = null;
        }
        while (true) {}
    }

	public static void test_2() {
        ThreadLocal<Integer> [] tls = new ThreadLocal[9];
        for (int i = 0; i < tls.length; i++) {
            tls[i] = new ThreadLocal<Integer>();
        }
        ThreadLocalMap map = new ThreadLocalMap(tls[0], 0);
        for (int i = 1; i < 9; i++) {
            System.out.print("i = " + i + ", hash = ");
            map.set(tls[i], i);
        }
        map.printEntry();
        tls[4] = null;
        System.gc();
        //map.set(tls[4], 4);
        System.out.println("---------------------------------");
        map.printEntry();
    }
	
	public static void test_1() {
		ThreadLocal<Integer> tl_1 = new ThreadLocal<>();
        ThreadLocal<Integer> tl_2 = new ThreadLocal<>();
        ThreadLocal<Integer> tl_3 = new ThreadLocal<>();
        ThreadLocal<Integer> tl_4 = new ThreadLocal<>();
        ThreadLocal<Integer> tl_5 = new ThreadLocal<>();
        Integer firstValue = 1;
        System.out.println(tl_1.threadLocalHashCode + ","
                + tl_2.threadLocalHashCode
                + "," + tl_3.threadLocalHashCode
                + "," + tl_4.threadLocalHashCode
                + "," + tl_5.threadLocalHashCode);
        ThreadLocalMap map = new ThreadLocalMap(tl_1, firstValue);
        //map.printEntry();
        map.set(tl_2, 2);
        map.set(tl_3, 3);
        map.set(tl_4, 4);
        map.set(tl_5, 5);
		map.printEntry();
	}
	
	/*
	public String toString() {
		return this + "-" + threadLocalHashCode;
	}
	*/
	
	private final int threadLocalHashCode = nextHashCode();
	
	private static AtomicInteger nextHashCode =
	        new AtomicInteger();
	
	private static final int HASH_INCREMENT = 0x61c88647;
	
	private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }
	
	T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }
	

    static class ThreadLocalMap {
    	
        public void printEntry () {
            for (int i = 0; i < table.length; i++) {
                System.out.println("table[" + i + "] = " + table[i]);
            }
        }

        static class Entry extends WeakReference<ThreadLocal<?>> {
            Object value;
            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
            public String toString() {
            		return "[" + get() + "," + value + "]";
            }
        }

        private static final int INITIAL_CAPACITY = 16;

        private Entry[] table;

        private int size = 0;

        private int threshold; // Default to 0

        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            System.out.format("i = 0, hash = %d & (%d - 1) = %d\n", firstKey.threadLocalHashCode, INITIAL_CAPACITY, i);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * 作用:  先用hash定位寻找key,如果找到key 返回该节点
         *       如果没有找到key 返回getEntryAfterMiss(key, i, e)的结果
         */
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1); //计算hash值
            Entry e = table[i];
            if (e != null && e.get() == key) // 如果命中
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }

        /**
         * 作用:  利用开发地址法寻找key,如果找到key 返回该节点
         *       如果没有找到key 返回null
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key) //找到key存在的位置,直接返回节点
                    return e;
                /**
                 *  如果当前节点的key过期,则调用expungeStaleEntry(i)进行清理当前位置
                 *  并且不接受返回值,i 没有发生变化
                 *
                 *  如果不过期则取下一个节点
                 */
                if (k == null)
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * 作用: 将key和value 插入(如果key不存在)或者更新(如果key存在)
         * @param key    键
         * @param value  值
         */
        private void set(ThreadLocal<?> key, Object value) {

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            System.out.format("%d & (%d - 1) = %d\n", key.threadLocalHashCode, len, i);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
                // 如果key存在,则替换该值
                if (k == key) {
                    e.value = value;
                    return;
                }
                /**
                 * 如果当前k过期,则调用replaceStaleEntry方法
                 * 无论key是否存在,都会保存在位置i,具体细节可以看replaceStaleEntry的注释
                 */

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
            /**
             *  有限次去查找过期节点并删除过期节点,如果有删除则返回
             *  如果没有删除则判断是否超过阀值
             *  如果超过阀值则调用rehash函数.
             */
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

        /**
         *
         * 作用: 删除key,调用了expungeStaleEntry(i)做清除和rehash工作,
         *
         * @param key 要删除的键值
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1); //获取hash值, 如果不在该位置则继续往下找直到遇到null
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i); //做清除工作
                    return;
                }
            }
        }

        /**
         *
         * 将set操作期间遇到的过期节点替换为指定键的节点。
         * 无论指定键的节点是否已存在，value参数中传递的值都存储在节点中。
         * 作为副作用，此方法将清除包含过期节点的“run”中的所有过期节点。 （run是两个空槽之间的一系列节点。）
         *
         *
         * @param key         节点的键
         * @param value       节点的值
         * @param staleSlot   在寻找key过程中遇到的第一个过期的节点
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                       int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            /**
             * 备份以检查当前"run"中的先前失效节点。
             * 我们一次清理整个"run"，以避免由于垃圾收集器释放串联的refs（即，每当收集器运行时）不断的增量重复。
             * slotToExpung 始终代表着整个run里面的第一个过期节点.
             */
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = prevIndex(i, len))
                if (e.get() == null)
                    slotToExpunge = i;

            /**
             *   寻找"run"中的key 或者第一个空节点(null)
             *   1. 找到key的位置i,就交换tab[i]和tab[staleSlot],提高查找时候的命中率
             *   2. 如果找到一个空节点,就表示该key之前没有插入到该tab中过,跳出循环后创建一个新的节点(key,value)
              */

            for (int i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                /**
                 * 如果我们找到键，那么我们需要将它与陈旧条目交换以维护哈希表顺序。
                 * 新陈旧的插槽或任何其他过期的插槽,在它上面遇到，然后可以发送到expungeStaleEntry
                 * 删除或重新运行run中的所有其他条目。
                 *
                 */
                if (k == key) {
                    e.value = value; // 替换value

                    tab[i] = tab[staleSlot];  // 交换
                    tab[staleSlot] = e;

                    /**
                     * 如果slotToExpunge == staleSlot
                     * 表明当前的i是整个run里面的第一个过期的元素节点,更新一下slotToExpunge即可.
                     */
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                /**
                 *  如果在反向扫描中找不到过期的节点, 那么在扫描key是看到的
                 *  第一个过期节点就是整个run里面的过期节点
                 */
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            /**
             *  如果key没有找到,表明该key是第一次存入到该table中,
             *  则生成一个新的节点并放到staleSlot的位置.
             */
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            /**
             * 如果staleSlot不是该run里面的唯一一个过期节点,
             * 则都需要进行清除工作
             */
            if (slotToExpunge != staleSlot)
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }

        /**
         *
         * 作用: 从该索引staleSlot往下直到遇到null结束返回当前下标,遇到的过期元素tab[i]设置为null,遇到的正常节点做rehash.
         * @param staleSlot 需要清理的位置, 一个已经确定过期的位置
         * @return 返回从staleSlot位置开始第一个为entry值为null的位置
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            /**
             *  清除该staleSlot的值
             */
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            size--;

            /**
             *  从stateSlot 开始往下继续搜索
             *  1. 如果为null, 直接退出
             *  2. 如果虚引用对应的key已经为null,也就是被垃圾回收器回收了,则清除该位置
             *  3. 如果不是1或者2,表明该位置存着一个正常值,观察是否需要rehash,因为取值的时候会方便
             *     因为该类处理hash冲突使用的是:开放定址法
             */
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    /**
                     *  因为处理冲突使用的开放地址法, 现在已经删除了一个位置,
                     *  并且该节点前面的节点有可能为null,因为k==null的时候会把tab[i]=null,
                     *  所以比如下次set操作对该key进行操作的时候就找不到该key,因为前面有null值,
                     *  会认为该key不存在,重新创建一个新的节点,因此会造成有两个节点拥有同一个key.
                     *
                     *  所以需要进行rehash
                     *
                     *  因此之前有些位置因为冲突没有存放到对应的hash值该有的位置,
                     *  所以下面的方法就是检查并且把此对象存到对应的hash值的位置或者它的后面.
                     */
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // 往下继续寻找,值到找到为null的空位置,然后把只放进去
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         *
         * @param i 从该位置i的下一个位置开始
         * @param n n >>>= 1决定尝试的次数
         * @return 返回是否有清除过陈旧的值
         */
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                // 获取下一个位置
                i = nextIndex(i, len);
                Entry e = tab[i];
                // 如果当前节点不为null,并且对应的key已经被垃圾回收器收集
                if (e != null && e.get() == null) {
                    // 重新设置n 和 设置removed标志位为true
                    n = len;
                    removed = true;
                    // 清除陈旧的位置节点i, 并设置i为当前i下一个位置开始第一个为entry值为null的位置
                    i = expungeStaleEntry(i);
                }
            } while ( (n >>>= 1) != 0);
            return removed;
        }

        /**
         * 作用:
         * 1. 先对整个数组的过期节点进行清除
         * 2. 判断是否需要对数组进行扩展
         */
        private void rehash() {
            /**
             * 先对整个数组的过期节点进行清除
             */
            expungeStaleEntries();

            /**
             *  size >= 0.75 * threshold 则扩大容量
             */

            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         *  作用: 扩展数组
         *  size扩大两倍, 每一个正常的元素做rehash映射到新的数组中
         *  每一个过期的元素的value都设置为null方便gc
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         *  作用: 从头到尾扫描整个数组对所有过期节点做清理工作
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    expungeStaleEntry(j);
            }
        }
    }
}
