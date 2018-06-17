import java.util.LinkedList;
import java.util.Queue;


public class RedBlackTree<Key extends Comparable<Key>, Value> {
	
	private static final boolean RED = true;
	private static final boolean BLACK = false;
	
	private Node root;       //头节点
	
	private class Node {
		Key key;              //用来比较的键
		Value value;          //用来保存真正的值
		Node left, right;     //左右子节点
		/* 是否是红节点 true表示红节点(与父亲节点的链接为红色链接) false表示黑节点(与父亲节点的链接为黑色链接) */
		boolean color;        
		Node(Key key, Value value, boolean color) {
			this.key = key;
			this.value = value;
			this.color = color;
		}
		
		public String toString() {
			return "[" + key + "," + value + "," + (color?"RED":"BLACK") + "]";
		}
	}
	
	private boolean isRed(Node h) {
		if (h == null) return false;
		return h.color == RED;
	}
	
	/* 左旋 */
	private Node rotateLeft(Node h) {
		/*旋转Node h,x*/
		Node x = h.right;
		h.right = x.left;
		x.left = h;
		
		/*交换Node h,x的颜色*/
		boolean h_color = h.color;
		h.color = x.color;
		x.color = h_color;
		
		/*返回旋转后新的取代h的节点*/
		return x;
	}
	
	/* 右旋 */
	private Node rotateRight(Node h) {
		/*旋转Node h,x*/
		Node x = h.left;
		h.left = x.right;
		x.right = h;
		
		/*交换Node h,x的颜色*/
		boolean h_color = h.color;
		h.color = x.color;
		x.color = h_color;
		
		/*返回旋转后新的取代h的节点*/
		return x;
	}
	
	/*  颜色转换  */
	private void flipColors(Node h) {
		h.color = !h.color;
		if (h.left != null) h.left.color = !h.left.color;
		if (h.right != null) h.right.color = !h.right.color;
	}
	
	private Node balance(Node h) { 
		if (h == null) return null;
		if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);  //情况1
		if (isRed(h.left) && (h.left != null && isRed(h.left.left))) h = rotateRight(h); //情况2
		if (isRed(h.left) && isRed(h.right)) flipColors(h); //情况3
		return h;
	}
	
	public void put(Key key, Value value) {
		root = put(root, key, value);
		root.color = BLACK;
	}
	
	private Node put(Node h, Key key, Value value) {
		if (h == null) return new Node(key, value, RED);
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) h.left = put(h.left, key, value);         // 一定要用h.left接受返回值,因为字树可能旋转更换了节点
		else if (cmp > 0) h.right = put(h.right, key, value);  // 一定要用h.right接受返回值,因为字树可能旋转更换了节点
		else h.value = value;                                  // 更新value值
		
		return balance(h);                                     //每一层都需要检查是否对当前节点有影响
	}
	
	private Node moveRedLeft(Node h) {
		flipColors(h);   // 构建4-节点有可能会产生5-节点
		if (h.right != null && isRed(h.right.left)) { //生成了5-节点
			h.right = rotateRight(h.right);
			h = rotateLeft(h);
			flipColors(h);  //分解5-节点 分解成2个2-节点和一个3-节点
		}
		return h;
	}
	
	public void deleteMin() {
		root = deleteMin(root);
		if (root != null) root.color = BLACK;
	}
	
	private Node deleteMin(Node h) {
		if (h == null) return null;
		if (h.left == null) return null;
		if (!isRed(h.left) && (h.left != null && !isRed(h.left.left))) { //如果左孩子和左孙子都是黑节点 需要构造节点
			h = moveRedLeft(h);
		}
		h.left = deleteMin(h.left);
		return balance(h);
	}
	
	private Node moveRedRight(Node h) {
		flipColors(h); // 构建4-节点有可能会产生5-节点
		if (h.left != null && isRed(h.left.left)) { //生成了5-节点
			h = rotateRight(h);
			flipColors(h);  //分解5-节点 分解成2个2-节点和一个3-节点
		}
		return h;
	}
	
	public void deleteMax() {
		root = deleteMax(root);
		if (root != null) root.color = BLACK;
	}
	
	private Node deleteMax(Node h) {
		if (h == null) return null;
		if (isRed(h.left)) h = rotateRight(h);
		if (h.right == null) {
			if (h.left == null) System.out.println("h.left is null");
			return null;
		}
		if (!isRed(h.right) && (h.right != null && !isRed(h.right.left))) {
			h = moveRedRight(h);
		}
		h.right = deleteMax(h.right);
		return balance(h);
	}
	
	private Node min (Node h) {
		if (h == null) return null;
		while (h.left != null) h = h.left;
		return h;
	}
	
	public void delete(Key key) {
		root = delete(root, key);
		if (root != null) root.color = BLACK;
	}
	
	private Node delete(Node h, Key key) {
		if (h == null) return null;
		if (key.compareTo(h.key) < 0) {
			if (!isRed(h.left) && (h.left != null && !isRed(h.left.left))) {
				h = moveRedLeft(h);
			}
			h.left = delete(h.left, key);
		} else {  // 不管是否是要删除的节点  为右侧构造红链接
			if (isRed(h.left)) h = rotateRight(h); // 先把红链接调整到右侧
			if (key.compareTo(h.key) == 0 && h.right == null) { //在根节点 类似于删除最大键
				return null;
			}
			if (!isRed(h.right) && !isRed(h.right.left)) { //为右侧构造3-节点或4-节点
				h = moveRedRight(h);
			}
			if (key.compareTo(h.key) == 0) {
				Node min_of_h_right = min(h.right);
				h.key = min_of_h_right.key;
				h.value = min_of_h_right.value;
				h.right = deleteMin(h.right);
			} else {
				h.right = delete(h.right, key);
			}
		}
		return balance(h);
	}
	
	public void another_delete(Key key) {
		root = another_delete(root, key);
		if (root != null) root.color = BLACK;
	}
	
	private Node another_delete(Node h, Key key) {
		if (h == null) return null;
		int cmp = key.compareTo(h.key);
		if (cmp < 0) {
			if (!isRed(h.left) && (h.left != null && !isRed(h.left.left))) {
				h = moveRedLeft(h);
			}
			h.left = another_delete(h.left, key);
		} else if (cmp > 0) {
			if (isRed(h.left)) h = rotateRight(h); // 先把红链接调整到右侧
			if (!isRed(h.right) && !isRed(h.right.left)) { //为右侧构造3-节点或4-节点
				h = moveRedRight(h);
			}
			h.right = another_delete(h.right, key);
		} else {
			if (h.left == null) {
				/*那h.right必然要么是一个红链接,要么为null,因为需要保持黑色平衡性
				 * 如果出现两个节点,那就必然两个红链接连一起不可能出现
				 * 如果一个
				 */
				if (h.right != null) h.right.color = h.color;
				h = h.right;
			} else if (h.right == null) {
				if (h.left != null) h.left.color = h.color;
				h = h.left;
			} else {
				Node min_of_h_right = min(h.right);
				h.key = min_of_h_right.key;
				h.value = min_of_h_right.value;
				h.right = deleteMin(h.right);
			}
		}
		return balance(h);
	}
	
	public Value get(Key key) {
		return get(root, key);
	}
	
	private Value get(Node h, Key key) {
		if (h == null) return null;
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) return get(h.left, key);
		else if (cmp > 0) return get(h.right, key);
		return h.value;
	}
	
	
	public void layerTraverse() {
		layerTraverse(root);
	}
	
	
	/* 
	 *    横向遍历
	 */
	private void layerTraverse(Node h) {
		if (h == null) return;
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(h);
		while (!queue.isEmpty()) {
			Queue<Node> tmp = new LinkedList<Node>();
			while (!queue.isEmpty()) {
				Node cur = queue.poll();
				System.out.print(cur + " ");
				if (cur != null) {
					tmp.add(cur.left);
					tmp.add(cur.right);
				}
			}
			queue = tmp;
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		RedBlackTree<Character, Integer> rbt = new RedBlackTree<Character, Integer>();
		char[] inserts = "SEARCHXMPL".toCharArray();
		for (int i = 0; i < inserts.length; i++) {
			rbt.put(inserts[i], i);
		}
		rbt.layerTraverse();
		
		/*
		while (rbt.root != null) {6
			rbt.deleteMin();
			System.out.println("\n---------------------");
			rbt.layerTraverse();
		}
		
		
		while (rbt.root != null) {
			rbt.deleteMax();
			System.out.println("\n---------------------");
			rbt.layerTraverse();
		}
		
		*/
		
		char[] dels = "MXCEHA".toCharArray();
		for (int i = 0; i < dels.length; i++) {
			rbt.another_delete(dels[i]);
			System.out.println("\n---------------------");
			rbt.layerTraverse();
		}
		
		System.out.println(rbt.get('L'));
		System.out.println(rbt.get('A'));
	}

}
