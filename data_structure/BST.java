import java.util.LinkedList;
import java.util.Queue;

import com.collection.Test;

public class BST<Key extends Comparable<Key>, Value> {
	private Node root;
	
	private class Node {
		private Key key;
		private Value value;
		private Node left, right;
		
		public Node(Key key, Value value) {
			this.key = key;
			this.value = value;
		}
		
		public String toString() {
			return "[" + key + "," + value + "]";
		}
	}
	
	public void put(Key key, Value value) {
		root = put(root, key, value);
	}
	
	private Node put(Node h, Key key, Value value) {
		if (h == null) return new Node(key, value);
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) h.left = put(h.left, key, value);  //左子树
		else if (cmp > 0) h.right = put(h.right, key, value); //右子树
		else h.value = value;
		
		return h;
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
	
	
	public Value get(Key key) {
		return get(root, key);
	}
	
	public Value get(Node h, Key key) {
		if (h == null) return null;
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) return get(h.left, key);
		else if (cmp > 0) return get(h.right, key);
		else return h.value;
	}
	
	public void deleteMin () {
		root = deleteMin(root);
	}
	
	public Node deleteMin(Node h) {
		if (h == null) return null;          //如果是空树 直接返回
		if (h.left == null) return h.right;  //如果找到最小键 直接返回最小键的右孩子
		h.left = deleteMin(h.left);          //当前节点的左孩子等于当前节点的左子树删除最小键返回的子树根节点
		return h;
	}
	
	
	private Node min (Node h) {
		if (h == null) return null;
		while (h.left != null) h = h.left;
		return h;
	}
	
	public void delete (Key key) {
		root = delete(root, key);
	}
	
	private Node delete(Node h, Key key) {
		if (h == null) return null;
		int cmp = key.compareTo(h.key);
		
		if (cmp < 0) h.left = delete(h.left, key);
		else if (cmp > 0) h.right = delete(h.right, key);
		else {
			if (h.left == null) return h.right;  // 情况1
			if (h.right == null) return h.left;  // 情况2
			/* 情况3 */
			Node min_of_h_right = min(h.right);   // 当前节点h右子树的最小键
			Node root_of_h_right = deleteMin(h.right); // 当前节点h右子树删除最小键后的根节点
			min_of_h_right.right = root_of_h_right;  // 最小键的右孩子 是 h右子树删除最小键后的根节点
			min_of_h_right.left = h.left;            // 最小键的左孩子 是 h的左孩子
			h = min_of_h_right;                      // 把当前节点设为最小键 然后返回给上一层
		}
		return h;
	}
	
	public static void main(String[] args) {
		BST<String, Integer> bst = new BST<String, Integer>();
		bst.put("S", 0);
		bst.put("E", 1);
		bst.put("A", 2);
		bst.put("R", 3);
		bst.put("C", 4);
		bst.put("H", 5);
		bst.put("E", 6);
		bst.put("X", 7);
		bst.put("M", 8);
		bst.layerTraverse();
		
		System.out.println(bst.get("X"));
		System.out.println(bst.get("L"));
		
		
		
		bst.delete("E");
		bst.layerTraverse();
		
		bst.deleteMin();
		bst.layerTraverse();
	}
	
}
