import java.util.LinkedList;
import java.util.Queue;


public class BinaryBalancedTree<Key extends Comparable<Key>, Value> {

	private Node root;
	
	private class Node {
		Key key;               
		Value value;           
		Node left, right;
		int height;
		
		public Node(Key key, Value value) {
			this.key = key;
			this.value = value;
		}
		
		public String toString() {
			return "[" + key + "," + value + "," + height + "]";
		}
	}
	
	private int height(Node h) {
		return h == null ? -1 : h.height;
	}
	
	private int updateHeight(Node h) {
		return Math.max(height(h.left), height(h.right)) + 1;
	}
	
	/*  右旋   */
	private Node rotateRight(Node h) {
		Node x = h.left;
		h.left = x.right;
		x.right = h;
		
		h.height = updateHeight(h);
		x.height = updateHeight(x); //h,x顺序不能变
		return x;
	}
	
	/*  左旋   */
	private Node rotateLeft(Node h) {
		Node x = h.right;
		h.right = x.left;
		x.left = h;
		
		h.height = updateHeight(h);
		x.height = updateHeight(x); //h,x顺序不能变
		return x;
	}
	
	/*左-右旋转*/
	private Node rotateLeftRight(Node h) {
		h.left = rotateLeft(h.left);
		return rotateRight(h);
	}
	
	/*右-左旋转*/
	private Node rotateRightLeft(Node h) {
		h.right = rotateRight(h.right);
		return rotateLeft(h);
	}
	
	
	public void put(Key key, Value value) {
		root = put(root, key, value);
	}
	
	private Node put(Node h, Key key, Value value) {
		if (h == null) return new Node(key, value);
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) {
			h.left = put(h.left, key, value);
			if (height(h.left) - height(h.right) == 2) { //出现不平衡 只会是左子树比右子树高2
				if (key.compareTo(h.left.key) < 0) { // h.左孩子的左子树
					h = rotateRight(h);  //对h进行右旋转
				} else {
					h = rotateLeftRight(h); // 对h进行左-右旋转
				}
			}
		} else if (cmp > 0) {
			h.right = put(h.right, key, value);
			if (height(h.right) - height(h.left) == 2) { //出现不平衡 只会是右子树比左子树高2
				if (key.compareTo(h.right.key) > 0) { // h.右孩子的右子树
					h = rotateLeft(h);      //对h进行左旋转
				} else {
					h = rotateRightLeft(h);
				}
			}
		} else {  // 更新value
			h.value = value;
		}
		
		h.height = updateHeight(h);
		return h;
	}
	
	public void deleteMin() {
		root = deleteMin(root);
	}
	
	private Node deleteMin(Node h) {
		if (h == null) return null;
		if (h.left == null) return h.right;
		h.left = deleteMin(h.left);
		if (height(h.right) - height(h.left) == 2) {
			h = rotateLeft(h);
		} 
		return h;
	}
	
	public Node min(Node h) {
		if (h == null) return h;
		while (h.left != null) h = h.left;
		return h;
	}
	
	public void delete (Key key) {
		root = delete(root, key);
	}
	
	private Node delete(Node h, Key key) {
		if (h == null) return null;
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) {
			h.left = delete(h.left, key);
			if (height(h.right) - height(h.left) == 2) { //出现不平衡 只会是右子树比左子树高2
				h = rotateLeft(h);
			}
		} else if (cmp > 0) {
			h.right = delete(h.right, key);
			if (height(h.left) - height(h.right) == 2) { //出现不平衡 只会是右子树比左子树高2
				h = rotateRight(h);
			}
		} else {
			if (h.left == null) return h.right;
			if (h.right == null) return h.left;
			
			Node min = min(h.right);
			min.right = deleteMin(h.right);
			min.left = h.left;
			
			h = min;
			
			if (height(h.left) - height(h.right) == 2) {
				h = rotateRight(h);
			}
		}
		
		h.height = updateHeight(h);
		return h;
	}
	
	public Value get(Key key) {
		return get(root, key);
	}
	
	private Value get(Node h, Key key) {
		if (h == null) return null;
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) return get(h.left, key);
		else if (cmp > 0) return get(h.right, key);
		else return h.value;
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
		BinaryBalancedTree<String, Integer> bst = new BinaryBalancedTree<String, Integer>();
		bst.put("A", 0);
		bst.put("B", 1); 
		bst.put("C", 2);
		bst.put("D", 3);
		bst.put("E", 4);
		bst.put("F", 5);
		bst.put("G", 6);
		bst.layerTraverse();
		
		bst.delete("D");
		bst.layerTraverse();
		
		bst.delete("E");
		bst.layerTraverse();
		
		bst.delete("F");
		bst.layerTraverse();
	}

}
