
import java.util.LinkedList;
import java.util.Queue;

public class RBTree<Key extends Comparable<Key>, Value> {
	
	private static final boolean RED = true;
	private static final boolean BLACK = false;
	
	private Node root;       //头节点
	
	private class Node {
		Key key;              //用来比较的键
		Value value;          //用来保存真正的值
		Node left, right;     //左右子节点
		/* 是否是红节点 true表示红节点(与父亲节点的链接为红色链接) false表示黑节点(与父亲节点的链接为黑色链接) */
		Node parent;
		boolean red;        
		Node(Key key, Value value, boolean color) {
			this.key = key;
			this.value = value;
			this.red = color;
		}
		
		public String toString() {
			return "[" + key + "," + value + "," + (red?"RED":"BLACK") + "]";
		}
	}
	
	private boolean isRed(Node h) {
		if (h == null) return false;
		return h.red == RED;
	}
	/* 左旋转 */
	private Node rotateLeft(Node root, Node p) {
		Node r, pp, rl;
		if (p != null && (r = p.right) != null) {
			if ((rl = p.right = r.left) != null) rl.parent = p;
			if ((pp = r.parent = p.parent) == null) {
				(root = r).red = false;
			} else if (pp.right == p) {
				pp.right = r;
			} else {
				pp.left = r;
			}
			p.parent = r;
			r.left = p;
		}
		return root;
	}
	/* 右旋转 */
	private Node rotateRight(Node root, Node p) {
		Node l, pp, lr;
		if (p != null && (l = p.left) != null) {
			if ((lr = p.left = l.right) != null) lr.parent = p;
			if ((pp = l.parent = p.parent) == null) (root = l).red = false;
			else if (pp.left == p) pp.left = l;
			else pp.right = l;
			
			l.right = p;
			p.parent = l;
		}
		return root;
	}
	
	public void put(Key key, Value value) {
		root = put(root, key, value);
		if (root != null) root.red = BLACK;
	}
	
	/* 把key插入到红黑树中, */
	private Node put(Node h, Key key, Value value) {
		if (h == null) return new Node(key, value, RED);
		for (Node p = h; ; ) {
			int cmp = key.compareTo(p.key);
			Node xp = p;
			if ((p = (cmp < 0 ? p.left : p.right)) == null || cmp == 0) {  //生成节点 起始节点颜色是红色
				if (cmp == 0) {   //更新节点信息
					xp.value = value;
					break;
				}
				Node node = new Node(key, value, RED); 
				if (cmp < 0) xp.left = node;  //新生成的节点在左侧
				else xp.right = node;         //新生成的节点在右侧
				node.parent = xp;
				
				root = balanceInsertion(root, node); //调整节点
				break;
			}
		}
		return root;
	}
	
	/**
     * 
     * @param root 红黑树的根节点(注意红黑树根节点不一定是链表的头节点)
     * @param x    新插入到红黑树中的节点
     * @return     返回红黑树的根节点
     */
    private Node balanceInsertion(Node root, Node x) {
    		/*
    		 * 先将新插入的节点x的元素设置为红色  (关于红黑树的基本知识如果有不理解的可以参考我的另一篇博客图解红黑树)
    		 * xp = x.parent x的父亲
    		 * xpp = x.parent.parent x的父亲的父亲
    		 * xppl = x.parent.parent.left x的父亲的父亲的左孩子
    		 * xppr = x.parent.parent.right x的父亲的父亲的右孩子
    		 * 插入的时候大情况分为4种:
    		 * 1. xp是null,表明x是根节点,因此直接把x的颜色设置为黑色返回即可
    		 * 2. xp是黑节点或者xp是根节点都可以直接返回根节点,简单解释一下:
    		 *    - 如果xp是黑节点,又因为插入的x是红节点,因此不会影响整棵树的平衡性可以直接返回
    		 *    - 如果xp是根节点,此时不管xp是黑节点或者是红节点,最终xp都会变成黑节点因为它是根节点,所以也可以直接返回
    		 * Note:如果1,2都没有发生,那么此时需要注意到x是红节点 xp也是红节点 还有一点因为xp是红节点,那xpp必然是黑节点
    		 * 3. 如果xp是xpp的左链接
    		 *    3(1).如果xpp的右链接xppr是红节点那表明xpp的两个孩子同时是红节点,因此直接颜色转换,然后x=xpp依次向上递归
    		 *    3(2).如果xppr不是红节点
    		 *    	   3(2)(1). 如果x是xp的右孩子,那需要通过一次左旋转来把2个红节点旋转在左边
    		 *         3(2)(2). (不管是通过旋转到左边还是本身就是在左边)目前两个2节点都在左边,通过一次右旋转后继续for循环操作
    		 * 4. 如果xp是xpp的右链接
    		 *    3(1).如果xpp的左链接xppl是红节点那表明xpp的两个孩子同时是红节点,因此直接颜色转换,然后x=xpp依次向上递归
    		 *    3(2).如果xppl不是红节点
    		 *    	   3(2)(1). 如果x是xp的左孩子,那需要通过一次右旋转来把2个红节点旋转在右边
    		 *         3(2)(2). (不管是通过旋转到右边还是本身就是在右边)目前两个2节点都在右边,通过一次左旋转继续for循环操作
    		 *      
    		 */
        x.red = true;
        for (Node xp, xpp, xppl, xppr;;) {
            if ((xp = x.parent) == null) {                       // 情况1
                x.red = false;
                return x;
            }
            else if (!xp.red || (xpp = xp.parent) == null)       // 情况2
                return root;
            if (xp == (xppl = xpp.left)) {                       // 情况3
                if ((xppr = xpp.right) != null && xppr.red) {    // 情况3(1)
                    xppr.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                }
                else {                                           // 情况3(2)
                    if (x == xp.right) {                         // 情况3(2)(1)
                        root = rotateLeft(root, x = xp);
                        xpp = (xp = x.parent) == null ? null : xp.parent;
                    }
                    if (xp != null) {                           // 情况3(2)(2)
                        xp.red = false;
                        if (xpp != null) {
                            xpp.red = true;
                            root = rotateRight(root, xpp);
                        }
                    }
                }
            }
            else {                                             // 情况4
                if (xppl != null && xppl.red) {                // 情况4(1)
                    xppl.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                }
                else {                                          // 情况4(2)
                    if (x == xp.left) {                         // 情况4(2)(1)
                        root = rotateRight(root, x = xp);
                        xpp = (xp = x.parent) == null ? null : xp.parent;
                    }
                    if (xp != null) {                           // 情况4(2)(2)
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
	
	private Node findNode(Key key) {
		Node p;
		for (p = root; p != null; ) {
			int cmp = key.compareTo(p.key);
			if (cmp < 0) p = p.left;
			else if (cmp > 0) p = p.right;
			else return p;
		}
		return null;
	}
	
	public void remove(Key key) {
		Node del = findNode(key);
		root = removeTreeNode(del);
	}
	
	final Node removeTreeNode(Node h) {
		if (h == null) return null;
		// 在红黑树中删除 删除后后继点放在replacement
		Node p = h, pl = h.left, pr = h.right, replacement;
		if (pl != null && pr != null) {
			/**
			 * 整个部分就是在做一件事:就是把p与后继节点交换然后变化成了删除情况的1,2 并且找到对应的替代点replacement
			 */
			Node s = pr, sl;
			while ((sl = s.left) != null) 
				s = sl;
			boolean c = s.red;
			s.red = p.red;
			p.red = c; // 交换颜色
			Node sr = s.right;
			Node pp = p.parent;
			if (s == pr) { // p是s的直接父亲
				p.parent = s;
				s.right = p;
			} else {
				Node sp = s.parent;
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
			if (sr != null)     //如果找到的后继节点的右节点不为空(因为后继节点的左节点肯定是空的),替代节点就为后继节点的右孩子
				replacement = sr;
			else                //如果后继节点左右孩子都是空 那替代节点就暂时是它本身
				replacement = p;
		} else if (pl != null)  //如果右节点为空 替代节点是左节点
			replacement = pl;
		else if (pr != null)    //如果左节点为空 替代节点是右节点
			replacement = pr;
		else                    //如果左右节点都空 替代节点是其本身
			replacement = p;
		/**
		 * 如果替代节点不是本身的话就可以直接删除p了,因为后续调整的事情跟p没有任何关系了
		 * 如果替代节点是本身的话需要先做完调整然后再删除
		 */
		if (replacement != p) {  
			Node pp = replacement.parent = p.parent;
			if (pp == null)
				root = replacement;
			else if (p == pp.left)
				pp.left = replacement;
			else
				pp.right = replacement;
			p.left = p.right = p.parent = null;
		}
		
		Node r = p.red ? root : balanceDeletion(root, replacement);

		if (replacement == p) { // 删除p
			Node pp = p.parent;
			p.parent = null;
			if (pp != null) {
				if (p == pp.left)
					pp.left = null;
				else if (p == pp.right)
					pp.right = null;
			}
		}
		return root;
	}
	
	private Node balanceDeletion(Node root, Node x) {
		for (Node xp, xpl, xpr;;) {
			if (x == null || x == root)
				return root;
			else if ((xp = x.parent) == null) {
				x.red = false;
				return x;
			} else if (x.red) {
				x.red = false;
				return root;
			} else if ((xpl = xp.left) == x) { // 左链接
				if ((xpr = xp.right) != null && xpr.red) {   //情况1:黑-黑 x的兄弟节点是红色
					xpr.red = false;
					xp.red = true;
					root = rotateLeft(root, xp);            
					xpr = (xp = x.parent) == null ? null : xp.right;  //更新xp和xpr
				}
				if (xpr == null)
					x = xp;
				else {
					Node sl = xpr.left, sr = xpr.right;
					if ((sr == null || !sr.red) && (sl == null || !sl.red)) {  //情况2:黑-黑 x的兄弟节点xpr是黑色并且它的两个字节点是黑色的
						xpr.red = true;
						x = xp;
					} else {
						if (sr == null || !sr.red) {  //情况3:黑-黑 x的兄弟节点xpr是黑色并且它的左孩子是红色,右孩子是黑色或者空
							if (sl != null)
								sl.red = false;
							xpr.red = true;
							root = rotateRight(root, xpr);
							xpr = (xp = x.parent) == null ? null : xp.right;
						}
						if (xpr != null) {             //情况4:黑-黑 x的兄弟节点xpr是黑色并且它的右孩子是红色,左孩子可以为任意颜色或者空
							xpr.red = (xp == null) ? false : xp.red;
							if ((sr = xpr.right) != null)
								sr.red = false;
						}
						if (xp != null) {
							xp.red = false;
							root = rotateLeft(root, xp);
						}
						x = root;   //可以直接到根节点了
					}
				}
			} else { // symmetric
				if (xpl != null && xpl.red) {
					xpl.red = false;
					xp.red = true;
					root = rotateRight(root, xp);
					xpl = (xp = x.parent) == null ? null : xp.left;
				}
				if (xpl == null)
					x = xp;
				else {
					Node sl = xpl.left, sr = xpl.right;
					if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
						xpl.red = true;
						x = xp;
					} else {
						if (sl == null || !sl.red) {
							if (sr != null)
								sr.red = false;
							xpl.red = true;
							root = rotateLeft(root, xpl);
							xpl = (xp = x.parent) == null ? null : xp.left;
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
		RBTree<Character, Integer> rbt = new RBTree<Character, Integer>();
		char[] inserts = "SEARCHXMPL".toCharArray();
		for (int i = 0; i < inserts.length; i++) {
			rbt.put(inserts[i], i);
		}
		rbt.layerTraverse();
		System.out.println("------------------");
		rbt.remove('A');
		rbt.layerTraverse();
		System.out.println("------------------");
		rbt.remove('L');
		rbt.layerTraverse();
		
		System.out.println("------------------");
		rbt.remove('C');
		rbt.layerTraverse();
		
		System.out.println("------------------");
		rbt.remove('P');
		rbt.layerTraverse();
	}
	
}
