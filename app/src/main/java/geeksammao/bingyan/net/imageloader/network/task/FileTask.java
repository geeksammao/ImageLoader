package geeksammao.bingyan.net.imageloader.network.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.callback.ImageLoadCallback;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class FileTask extends BaseTask {
    private String uri;
    private Handler handler;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;
    private ImageLoadCallback callback;
    private ImageView imageView;

    public FileTask(String uri, Handler handler, MemoryLRUCache<String, Bitmap> memoryLRUCache) {
        super();
        this.uri = uri;
        this.handler = handler;
        this.memoryLRUCache = memoryLRUCache;
    }

    @Override
    void startTask() {
        if (imageView != null) {
            final Bitmap bitmap = ImageUtil.decodeBitmapWithScale(imageView, uri);
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
            final Bitmap bitmap = BitmapFactory.decodeFile(uri);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onLoadCompleted(uri, bitmap);
                }
            });
            memoryLRUCache.put(uri, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null){
                callback.onLoadFailed(uri);
            }
        }
    }

    @Override
    public void setCallback(ImageLoadCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
