package org.commons.jconfig.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Thread safe Least Recently Used cache. The implementation allows for a LRU
 * cache logic, but the default is to evict the elements in FIFO order. This
 * reduces the computation and improves reading performance form the cache.
 * 
 * To implement the LRU logic it is necessary to call method touch(key) or call
 * method put(key, value) with the same [key, value] pair.
 * 
 * maxSize is an approximation.
 * 
 * 
 * @param <Key>
 * @param <Value>
 */
public class ConcurrentLRUCache<Key, Value> {
    private static final int MISS = 0;
    private static final int HIT = 1;
    private static final int REMOVED_KEYS = 2;
    private static final int REUSED_KEYS = 3;

    private final AtomicInteger mMaxSize = new AtomicInteger();

    private final ConcurrentHashMap<Key, Value> map;
    private final ConcurrentLinkedQueue<Key> queue;

    // Stats
    private final AtomicLongArray stats = new AtomicLongArray(4);

    /**
     * @param maxSize
     */
    public ConcurrentLRUCache(final int maxSize) {
        setMaxSize(maxSize);
        map = new ConcurrentHashMap<Key, Value>(maxSize);
        queue = new ConcurrentLinkedQueue<Key>();
    }

    /**
     * This constructor can be used to clone, reduce or increase the cache size.
     * 
     * @param maxSize
     * @param cache
     */
    public ConcurrentLRUCache(final int maxSize, final ConcurrentLRUCache<Key, Value> cache) {
        this(maxSize);
        for (Key key : cache.queue) {
            Value value = cache.get(key);
            if (null != value) {
                this.put(key, value);
            }
        }
    }

    /**
     * set the maxSize queue max size, to a bigger value
     * 
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        if (maxSize <= 1) {
            throw new IllegalArgumentException("Value " + maxSize + " has to be greater than zero.");
        }
        this.mMaxSize.set(maxSize);
    }

    /**
     * Touch a element in the cache, marks the element as recently used. On a
     * single thread maxSize will not be exceeded.
     * 
     * @param key
     *            - null key is not supported
     */
    public void touch(final Key key) {
        if (map.containsKey(key)) {
            synchronized (this) {
                // update queue age for passed key
                queue.remove(key);
                queue.add(key);
                stats.incrementAndGet(REUSED_KEYS);
            }
        }
    }

    /**
     * Insert a element in the cache. The tries to use the maxSize as an
     * approximation, the cache can grow a bit more than maxSize. The higher
     * concurrency on puts, the higher the probability of maxSize will be
     * exceeded. On a single thread maxSize will not be exceeded.
     * 
     * @param key
     *            - null key is not supported
     * @param val
     *            value Object
     */
    public void put(final Key key, final Value val) {
        if (map.containsKey(key)) {
            synchronized (this) {
                // update queue age for passed key
                queue.remove(key);
                queue.add(key);
                map.put(key, val);
                stats.incrementAndGet(REUSED_KEYS);
            }
            return;
        }

        // remove old keys to match the current Max Size.
        int maxSize = mMaxSize.get();
        while (queue.size() >= maxSize) {
            synchronized (this) {
                Key oldestKey = queue.poll();
                if (null != oldestKey) {
                    map.remove(oldestKey);
                    stats.incrementAndGet(REMOVED_KEYS);
                }
            }
        }

        synchronized (this) {
            queue.add(key);
            map.put(key, val);
        }
    }

    /**
     * Retrieve a value from cache
     * 
     * @param key
     *            - null key is not supported
     * @return key value
     */
    public Value get(final Key key) {
        Value v = map.get(key);
        if (v == null) {
            stats.incrementAndGet(MISS);
        } else {
            stats.incrementAndGet(HIT);
        }
        return v;
    }

    /**
     * Retrieve current size
     * 
     * @return size
     */
    public int size() {
        synchronized (this) {
            return queue.size();
        }
    }

    /**
     * clear cache
     * 
     * @return size
     */
    public void clear() {
        synchronized (this) {
            queue.clear();
            map.clear();
            // reset stats
            for (int i = 0; i < stats.length(); i++) {
                stats.set(i, 0);
            }
        }
    }

    /**
     * Returns a string with the LRU stats. This method is for testing only,
     * should be used as part of the api.
     * 
     * @return - a string with stats on the cache
     */
    public String getStats() {
        return "SIZE:" + queue.size() + ", MAX_SIZE:" + mMaxSize + ", HIT:" + stats.get(HIT) + ", MISS:"
                + stats.get(MISS) + ", REUSED_KEYS:" + stats.get(REUSED_KEYS) + ", REMOVED_KEYS:"
                + stats.get(REMOVED_KEYS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getStats();
    }
}
