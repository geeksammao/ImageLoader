package geeksammao.bingyan.net.imageloader.network.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class FileTask extends BaseTask {
    private String uri;
    private String actualUri;
    private Handler handler;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;

    public FileTask(String uri, Handler handler, MemoryLRUCache<String, Bitmap> memoryLRUCache) {
        // get the actual path
        this.uri = uri;
        this.actualUri = uri.substring(5);
        this.handler = handler;
        this.memoryLRUCache = memoryLRUCache;
    }

    @Override
    void startTask() {
        if (imageView != null) {
            final Bitmap bitmap = ImageUtil.decodeBitmapWithScale(imageView, actualUri);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
            memoryLRUCache.put(uri, bitmap);

            return;
        }

        try {
            final Bitmap bitmap = BitmapFactory.decodeFile(actualUri);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onLoadCompleted(uri, bitmap);
                }
            });
            memoryLRUCache.put(uri, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onLoadFailed(uri);
            }
        }
    }
}
