package geeksammao.bingyan.net.imageloader.core.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.InputStream;

import geeksammao.bingyan.net.imageloader.ImageLoader;
import geeksammao.bingyan.net.imageloader.cache.disk.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.memory.MemoryCache;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class AssetsTask extends LoadTask {
    private Context context;

    public AssetsTask(ImageLoader imageLoader, String uri, Handler handler, DiskCache diskCache, MemoryCache memoryCache) {
        super(imageLoader, uri, handler, diskCache, memoryCache);
    }

    public void setContext(Context context){
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
                memoryCache.put(uri, bitmap);

                return;
            }

            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onLoadCompleted(uri, bitmap);
                }
            });
            memoryCache.put(uri, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onLoadFailed(uri);
            }
        }
    }
}
