import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V> {
	
	/* 默认的数组的长度 或者说默认是buckets/bins的长度 */
	static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
	/* 最大长度 */
	static final int MAXIMUM_CAPACITY = 1 << 30;
	/* 默认的加载因子 用于计算阈值(threshold) */
	static final float DEFAULT_LOAD_FACTOR = 0.75f;
	/* 用于红黑树树化的节点个数阈值 */
	static final int TREEIFY_THRESHOLD = 8;
	/* 用于解除红黑树树化(将红黑树转换为链表)的节点个数阈值 */
	static final int UNTREEIFY_THRESHOLD = 6;
	/* 用于红黑树树化时要求数组的最小长度 */
	static final int MIN_TREEIFY_CAPACITY = 64;
	
	/*为什么有些属性设置为transient 在说序列化的时候会讲解 */
	/* 用于存节点的数组(节点会hash到table中的某一个index) */
	transient Node<K, V>[] table;
	/* 用于存HashMap中的所有节点 */
	transient Set<Map.Entry<K, V>> entrySet;
	/* 节点的总个数 */
	transient int size;
	/* 修改的次数 后续会看到modCount的作用 */
	transient int modCount;
	/* 决定是否扩容的阀值 */
	int threshold;
	/* 加载因子 用于计算阈值(threshold) */
	final float loadFactor;
	
	static class Node<K, V> implements Map.Entry<K, V> {
		final int hash;  // 节点的hash值
		final K key;     // 节点的键
		V value;         // 节点的值
		Node<K, V> next; // 指向下一个节点的指针
		
		/* 构造函数 */
		Node (int hash, K key, V value, Node<K, V> next) {
			this.hash  = hash;
			this.key   = key;
			this.value = value;
			this.next = next;
		}
		
		/* 重写getKey()方法 */
		@Override
		public K getKey() {
			// TODO Auto-generated method stub
			return key;
		}
		
		/* 重写getValue()方法 */
		@Override
		public V getValue() {
			// TODO Auto-generated method stub
			return value;
		}
		
		/* 重写setValue()方法 */
		@Override
		public V setValue(V newValue) {
			V oldValue = value;
			value = newValue;
			return oldValue;
		}
		
		/* 比较当前的节点与对象o是否属于同一个对象 final方法表示子类不能重写 */
		public final boolean equals(Object o) {
			if (o == this) return true;
			if (o instanceof Map.Entry) {
				Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
				if (Objects.equals(key, e.getKey()) &&
						Objects.equals(value, e.getValue()))
					return true;
			}
			return false;
		}
		
		public String toString() {
			return "[" + key.toString() + "->" + value.toString() + "]";
		}
	}
	
	/* 返回值是必须是2的幂 这个方法的返回值是大于等于cap的最小的2次幂
	 * 至于为什么必须是2的幂？在后续解析的hash和resize()会看到答案
	 * */
	static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
	
	/* 请注意并没有initialCapacity这个属性, 所以如何给数组初始化多大长度呢? 
	 * 下面的分析的resize()方法会给出答案.
	 * */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)   //检查是否有异常的数组长度赋值
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)          //如果大于最大容量,则直接赋值为最大容量
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))  //检查loadFactor是否有异常
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;                    //赋值loadFactor操作
        this.threshold = tableSizeFor(initialCapacity);  //根据初始容量计算出阀值
    }
    
    public HashMap(int initialCapacity) {  
        this(initialCapacity, DEFAULT_LOAD_FACTOR); //采用默认加载因子调用第一个构造函数
    }
    
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // 其余的属性都采用默认值
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

    /**
     * Returns k.compareTo(x) if x matches kc (k's screened comparable
     * class), else 0.
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }
    
 // Create a regular (non-tree) node
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // For conversion from TreeNodes to plain nodes
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // Create a tree bin node
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // 将Node节点转化成TreeNode节点
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }
    
    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }
	
    
	static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	/* put 方法 */
	public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
	
	/**
	 * hash  : key的hash值
	 * onlyIfAbsent : 为true的时候不改变原有值 为false时用value取代旧值 (在代码中会有体现)
	 * evict : 在HashMap的子类LinkedHashMap中会有用 到时候分析LinkedHashMap可以看到它的具体作用
	 * 
	 * */
	final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
		Node<K, V>[] tab;
		Node<K, V> p;
		int n, i;
		/** 如果tab还没有创建数组的话,则需要去resize方法中创建数组 
		 * resize()放到接下来解析 先继续看后面的逻辑
		 * */
		if ((tab = table) == null || (n = tab.length) == 0)
			n = (tab = resize()).length;
		/**
		 * 1. 首先根据hash值计算出该节点属于哪个bucket/bin,也就是index
		 *    i = (n - 1) & hash 其实就等于 hash%n (用位操作会快一点,这是n为2次幂的第一个用处)
		 *    假设 n = 16, hash = 31 -> i = 00001111&00011111=00001111=15
		 * 2. 如果此时的bucket是空,表明这个bucket还没有任何节点存入,
		 *    因此生成新节点后直接放入到该bucket
		 */
		if ((p = tab[i = (n - 1) & hash]) == null)
			tab[i] = newNode(hash, key, value, null);
		else {
			/**
			 * 进入else模块就说明 p此时不为null,所以这个节点应该放到这个bucket的后面
			 * 接下来如果这个节点之前有插入过,就会节点赋给e
			 * 有三种情况(互斥条件,要么1要么2要么3出现):
			 * 1. 此节点已经存在并且就在bucket的第一个位置,直接把p赋给e
			 * 2. p是一个TreeNode节点,(TreeNode是Node的一个间接子类,红黑树的分析会专门放到一个博客分析)
			 * 	  那表明这个bucket已经红黑树树化了,因此调用红黑树树化去插入或者更新
			 * 3. 在链表中,所以直接遍历链表即可,有一点需要注意,如果是新增加的节点要么链表中的数量就会增加一个
			 *    有可能会达到阀值,一旦到达阀值就需要调用treeifyBin方法树化,至于会不会树化已经怎么树化后面会
			 *    解析,这里先有个概念理解逻辑就可以
			 */
			Node<K, V> e; // 如果这个节点已经存入过,就会拿出那个节点并且赋给e
			K k;
			/* 情况1 */
			if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
				e = p;
			else if (p instanceof TreeNode)  /* 情况2 */
				e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
			else {
				/* 情况3 */
				for (int binCount = 0;; ++binCount) {             // 遍历链表并且记录链表个数
					if ((e = p.next) == null) {                   // 从链表的第二个开始,因为p在第一种情况已经比较过了
						p.next = newNode(hash, key, value, null); // 插入到链表尾
						if (binCount >= TREEIFY_THRESHOLD - 1)    // 树化 这个时候就可以看到TREEIFY_THRESHOLD的作用了
							treeifyBin(tab, hash);
						break;
					}
					if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) //表明链表中已经存在了这个节点
						break;
					p = e;
				}
			}
			if (e != null) {    // e不为null表明此节点已经存入过 所以这里没有modCount++
				V oldValue = e.value;
				//这里就可以很明确的看到onlyIfAbsent的作用,对了如果oldValue为null,那onlyIfAbsent就不起作用了
				if (!onlyIfAbsent || oldValue == null) 
					e.value = value;
				afterNodeAccess(e); // 用于子类LinkedHashMap的方法
				return oldValue;
			}
		}
		/**
		 *  进行到这里之前没有此(节点或者说key也行)存入过
		 */
		++modCount;                // modCount++
		if (++size > threshold)    // 先增加size,mappings的个数 判断是否需要扩容
			resize();
		afterNodeInsertion(evict); // 用于子类LinkedHashMap的方法
		return null;
	}
	
	/* 扩容 */
	final Node<K,V>[] resize() {
		/**
		 * oldTab 是 table
		 * oldThr 是 旧阀值
		 * oldCap 是 以前table的length,如果以前是null就为0
		 * 大情况为两种情况(有初始化过和第一次初始化)
		 * 1. 有初始化过: 这个时候oldCap会比0大
		 * 2. 第一次初始化: 分两种小情况
		 *    2(1). 有threshold值,其实也就是从构造函数中用initCapacity计算出来的threshold
		 *    2(2). 没有threshold,对应的空构造函数.
		 */
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {  // 对应情况1
            if (oldCap >= MAXIMUM_CAPACITY) {  //无法再进行扩容 直接把阀值设置为最大后返回
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)   //扩容两倍
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // 对应情况2(1).有参的两个构造函数会进入这个分支
            newCap = oldThr; // 这个知道为什么没有initCapacity也可以给table初始化长度了,newThr没有赋值
        else {               // 对应情况2(2).无参构造函数会进入这个分支
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {  // 如果新的阀值为0 则重新设置一下阀值
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;  //设置一下阀值
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap]; //初始化数组
        table = newTab;     //设置一下新table
        /**
         * 1. 如果第一次初始化的时候就直接返回了
         * 2. 不是的话就需要把所有在oldTab上的元素转移到新的table中
         */
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                	/**
                	 * 表明这个bucket里面有节点
                	 * 分为三个情况:
                	 * 1. 如果这个bucket只有一个节点, 那直接rehash一下放到新的table中就可以了
                	 * 2. 如果是树节点,那就由红黑树迁移(会写一个专门的博客分析HashMap的红黑树)
                	 * 3. 如果是链表,那就遍历链表转移,如何转移呢?
                	 *    首先简单介绍一下rehash,因为n=oldCap=table.length是2的幂,hash=key.hash&(n-1)
                	 *    由于新的table.length是2*oldCap,所以新hash=hash&(2*n-1)
                	 *    用二进制位表示(2n-1)也就是在以前的基础上加了一个1,所以与操作的时候就看key.hash的对应的那个位置是0还是1
                	 *    如果是0:那rehash的结果就没有改变
                	 *    如果是1:那rehash的结果就在原有的hash的结果上加上oldCap就可以了
                	 *    
                	 *    转移的时候把原来的链表分成两个链表:
                	 *    3(1): 结果为0的链表-loHead
                	 *    3(2): 结果为1的链表-hiHead
                	 *    最后两个链表头放到table对应的bucket中
                	 * 
                	 */
                    oldTab[j] = null;
                    if (e.next == null)                    // 情况1
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)        // 情况2
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else {                                 // 情况3
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {  //情况3(1)
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {                         //情况3(2)
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
	
	/* 将hash对应的bucket链表红黑树树化*/
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        /**
         *  有两个条件会先采用扩容而不是直接树化
         *  1. tab为null
         *  2. tab的length也就是capacity的大小比MIN_TREEIFY_CAPACITY=64小
         *     因为这个时候认为扩容的效果比树化要好
         */
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) 
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) { //如果当前bucket不为null
            TreeNode<K,V> hd = null, tl = null;
            /**
             * 循环遍历整个链表
             * 1. 先把Node节点转换成TreeNode节点
             * 2. 红黑树的所有节点按原来的顺序利用指针(prev和next)形成了一个双向链表
             *    这也是多次遍历链表的时候顺序也不会变化的原因,后续有专门的一个博客来分析HashMap的遍历
             */
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            /**
             * 将TreeNode形成的双向链表转化成红黑树
             */
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }
	
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	static final class TreeNode<K,V> extends Node<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }

        /**
         * Returns root of tree containing this node.
         */
        final TreeNode<K,V> root() {
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * Ensures that the given root is the first node of its bin.
         */
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                if (root != first) {
                    Node<K,V> rn;
                    tab[index] = root;
                    TreeNode<K,V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K,V>)rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }

        /**
         * Finds the node starting at root p with the given hash and key.
         * The kc argument caches comparableClassFor(key) upon first use
         * comparing keys.
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                          (kc = comparableClassFor(k)) != null) &&
                         (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }

        /**
         * Calls find for root node.
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

        /**
         * Forms tree of the nodes linked from this node.
         * @return root of tree
         */
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K,V> p = root;;) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K,V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from
         * this node.
         */
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
                Node<K,V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * Tree version of putVal.
         */
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * Removes the given node, that must be present before this call.
         * This is messier than typical red-black deletion code because we
         * cannot swap the contents of an interior node with a leaf
         * successor that is pinned by "next" pointers that are accessible
         * independently during traversal. So instead we swap the tree
         * linkages. If the current tree appears to have too few nodes,
         * the bin is converted back to a plain bin. (The test triggers
         * somewhere between 2 and 6 nodes, depending on tree structure).
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            int n;
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (root == null || root.right == null ||
                (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);  // too small
                return;
            }
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K,V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins,
         * or untreeifies if now too small. Called only from resize;
         * see above discussion about split bits and indices.
         *
         * @param map the map
         * @param tab the table for recording bin heads
         * @param index the index of the table being split
         * @param bit the bit of hash to split on
         */
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }
	
}
