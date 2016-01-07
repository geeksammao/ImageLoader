package geeksammao.bingyan.net.imageloader.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class MemoryLRUCache<K, V> {
    private int maxSize;
    private int size;
    private int hitCount;
    private int missCount;

    private final LinkedHashMap<K, V> map;

    public MemoryLRUCache(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("max size is less than 0");
        }
        this.maxSize = maxSize;
        map = new LinkedHashMap<>(0, 0.75f, true);
    }

    protected int sizeOf(K k,V v) {
        return 1;
    }

    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V value;
        synchronized (this) {
            value = map.get(key);
            if (value != null) {
                hitCount++;
                return value;
            }
            missCount++;
        }
        return null;
    }

    public final void put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null or value == null");
        }

        V previous;
        synchronized (this) {
            previous = map.put(key, value);
            size += sizeOf(key,value);

            // the item has existed in the map
            if (previous != null) {
                size -= sizeOf(key,value);
            }
        }

        trimToSize(maxSize);
    }

    public final void trimToSize(int maxSize) {
        while (true) {
            if (size <= maxSize) {
                return;
            }

            K key;
            V value;
            synchronized (this) {
                Map.Entry<K, V> toRemove = map.entrySet().iterator().next();
                if (toRemove == null) {
                    return;
                }

                key = toRemove.getKey();
                value = toRemove.getValue();
                map.remove(key);
                size -= sizeOf(key,value);
            }
        }
    }

    public final void remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V previous;
        synchronized (this) {
            previous = map.remove(key);
            if (previous != null) {
                size -= sizeOf(key,previous);
            }
        }
    }

    public final void resize(int maxSize) {
        synchronized (this) {
            this.maxSize = maxSize;
        }
        trimToSize(maxSize);
    }

    public final int getHitCount() {
        return hitCount;
    }

    public final int getMissCount() {
        return missCount;
    }
}
