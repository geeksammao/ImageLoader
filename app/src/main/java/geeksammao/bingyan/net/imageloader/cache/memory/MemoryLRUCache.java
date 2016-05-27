package geeksammao.bingyan.net.imageloader.cache.memory;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class MemoryLRUCache<K, V> implements MemoryCache{
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

    @Override
    public final Bitmap get(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        return lruCache.get(key);
    }

    @Override
    public final void put(String key, Bitmap value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null or value == null");
        }
        lruCache.put(key,value);
    }

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return lruCache.size();
    }

    @Override
    public int maxSize() {
        return lruCache.maxSize();
    }

    @Override
    public final void remove(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        lruCache.remove(key);
    }
}
