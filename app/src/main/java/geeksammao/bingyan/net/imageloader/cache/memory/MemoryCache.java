package geeksammao.bingyan.net.imageloader.cache.memory;

import android.graphics.Bitmap;

/**
 * Created by Geeksammao on 5/27/16.
 */
public interface MemoryCache {
    Bitmap get(String key);
    void put(String key,Bitmap bitmap);
    void remove(String key);
    void clear();
    int size();
    int maxSize();
}
