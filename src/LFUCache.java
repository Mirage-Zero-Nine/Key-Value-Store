package Mirage.LeetCodeSolution;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Design and implement a data structure for Least Frequently Used (LFU) cache.
 * It should support the following operations: get and put.
 * get(key) - Get the value (will always be positive) of the key if the key exists in the cache, otherwise return -1.
 * put(key, value) - Set or insert the value if the key is not already present.
 * When the cache reaches its capacity, it should invalidate the least frequently used item before inserting a new item.
 * For the purpose of this problem, when there is a tie (i.e., two or more keys that have the same frequency), the least recently used key would be evicted.
 *
 * @author BorisMirage
 * Time: 2018/09/29 20:59
 * Created with IntelliJ IDEA
 */

public class LFUCache {
    private HashMap<Integer, Integer> pair;       // store key-value pair
    private HashMap<Integer, Integer> count;      // store frequency as key-count pair
    private HashMap<Integer, LinkedHashSet<Integer>> frequency;        // determine which to be removed
    private int min = -1;
    private int capacity;

    /**
     * @param capacity cache capacity
     */
    public LFUCache(int capacity) {
        this.capacity = capacity;
        pair = new HashMap<>();
        count = new HashMap<>();
        frequency = new HashMap<>();
        frequency.put(1, new LinkedHashSet<>());     // first value

    }

    /**
     * <code>get</code> operation. Return -1 if key is not found in cache.
     *
     * @param key requesting key
     * @return corresponding value, or -1.
     */
    public int get(int key) {

        /* Key does not exist */
        if (!pair.containsKey(key)) {
            return -1;
        }
        int f = count.get(key);

        /* Record frequency */
        count.put(key, f + 1);

        /* Update hash map frequency for later eviction policy */
        frequency.get(f).remove(key);        // current key's frequency has updated, hence need to reassign
        if (f == min && frequency.get(f).size() == 0) {
            min++;      // Update min if min frequency in map has changed
        }
        if (!frequency.containsKey(f + 1)) {
            frequency.put(f + 1, new LinkedHashSet<>());
        }
        frequency.get(f + 1).add(key);

        return pair.get(key);
    }

    /**
     * <code>put</code> operation, put new key-value pair into cache.
     * If cache is oversize, it will remove Least Recently Used (LRU) Node store in cache.
     *
     * @param key   new key
     * @param value new value
     */
    public void put(int key, int value) {

        if (capacity < 1) {
            return;
        }

        if (pair.containsKey(key)) {
            pair.put(key, value);
            get(key);
            return;
        }
        if (pair.size() >= capacity) {
            int removeLeast = frequency.get(min).iterator().next();
            frequency.get(min).remove(removeLeast);
            count.remove(removeLeast);
            pair.remove(removeLeast);
        }
        pair.put(key, value);
        count.put(key, 1);
        min = 1;
        frequency.get(1).add(key);
    }

    public static void main(String[] args) {
        LFUCache testCache = new LFUCache(2);

        testCache.put(1, 1);
        testCache.put(2, 2);
        System.out.println(testCache.get(1));
        testCache.put(3, 3);
        System.out.println(testCache.get(2));
        System.out.println(testCache.get(3));
        testCache.put(4, 4);
        System.out.println(testCache.get(1));
        System.out.println(testCache.get(3));
        System.out.println(testCache.get(4));
        System.out.println(testCache);
    }
}