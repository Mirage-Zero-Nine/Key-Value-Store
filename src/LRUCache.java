import java.util.HashMap;

/**
 * @author BorisMirage
 * Time: 2018/09/30 11:06
 * Created with IntelliJ IDEA
 */

public class LRUCache {
    private int capacity;
    private Node head;
    private Node end;
    private int c = 0;      // count total cache size
    private HashMap<String, Node> cache = new HashMap<>();

    /**
     * Structure of cache:
     * Basically, use node to store key and value. A hash map in this cache is to store key-node pair for searching key.
     * Node works as a double linked list, which contains previous Node and next Node.
     * When <code>put</code> operation finds a existing key, move corresponding node to top of the list.
     * When put a new pair into cache, first check size to avoid oversize, then add this node to top of list.
     * If cache is oversize, then remove last Node in double linked list. And remove corresponding key as well.
     *
     * @param capacity cache capacity
     */
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.head = new Node();
        head.previous = null;
        this.end = new Node();
        end.next = null;
        head.next = end;
        end.previous = head;
    }

    /**
     * <code>get</code> operation. Return null if key is not found in cache.
     *
     * @param key requesting key
     * @return corresponding value, or null.
     */
    public String get(String key) {
        Node temp = cache.get(key);
        if (temp == null) {
            return null;
        }
        lastUsed(temp);
        return temp.val;
    }

    /**
     * <code>put</code> operation, put new key-value pair into cache.
     * If cache is oversize, it will remove Least Recently Used (LRU) Node store in cache.
     *
     * @param key   new key
     * @param value new value
     */
    public void put(String key, String value) {

        Node node = cache.get(key);

        if (node == null) {
            Node add = new Node();
            add.key = key;
            add.val = value;
            c++;
            if (c > capacity) {
                cache.remove(popEnd().key);
                addNode(add);
                cache.put(key, add);
                c--;
            } else {
                addNode(add);
                cache.put(key, add);
            }
        } else {
            cache.get(key).val = value;
            this.lastUsed(cache.get(key));
        }
    }

    /**
     * Remove given Node.
     *
     * @param node given Node
     */
    private void removeNode(Node node) {
        Node pre = node.previous;
        Node next = node.next;
        pre.next = next;
        next.previous = pre;
    }

    /**
     * Move given node next to head node.
     * Note that this node will be removed.
     *
     * @param node
     */
    private void lastUsed(Node node) {
        removeNode(node);
        addNode(node);
    }

    /**
     * Add a new Node next to head.
     *
     * @param node Node to be moved
     */
    private void addNode(Node node) {
        node.previous = head;
        node.next = head.next;
        head.next.previous = node;
        head.next = node;
    }

    /**
     * Remove last Node.
     * Hash map needs to know which key to remove, hence pop node is required.
     *
     * @return pop node
     */
    private Node popEnd() {
        Node old = end.previous;
        removeNode(old);
        return old;
    }
}

/**
 * Definition of Node.
 * Worked as double linked list.
 */
class Node {
    String key;
    String val;
    Node previous;
    Node next;
}
