package geeksammao.bingyan.net.imageloader.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class MemoryLRUCache<K, V> {
    private int maxSize;
    private LruCache<String, Bitmap> lruCache;

    public MemoryLRUCache(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("max size is less than 0");
        }
        this.maxSize = maxSize;
        lruCache = new LruCache<String,Bitmap>(maxSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    protected int sizeOf(final K k, final V v) {
        return 1;
    }

    public final Bitmap get(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        return lruCache.get(key);
    }

    public final void put(String key, Bitmap value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null or value == null");
        }
        lruCache.put(key,value);
    }

    public final void remove(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        lruCache.remove(key);
    }
}
