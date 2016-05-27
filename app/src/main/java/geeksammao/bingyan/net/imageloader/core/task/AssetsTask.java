package geeksammao.bingyan.net.imageloader.core.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.InputStream;

import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class AssetsTask extends LoadTask {
    private String uri;
    private Handler handler;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;
    private Context context;

    public AssetsTask(String uri, Handler handler, MemoryLRUCache<String, Bitmap> memoryLRUCache, Context context) {
        this.uri = uri;
        this.handler = handler;
        this.memoryLRUCache = memoryLRUCache;
        this.context = context;
    }

    @Override
    void startTask() {
        try {
            InputStream inputStream = context.getAssets().open(uri);

            if (imageView != null) {
                final Bitmap bitmap = ImageUtil.decodeBitmapWithScale(imageView, inputStream);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
                memoryLRUCache.put(uri, bitmap);

                return;
            }

            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
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
