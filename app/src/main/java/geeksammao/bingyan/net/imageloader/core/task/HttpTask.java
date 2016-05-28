package geeksammao.bingyan.net.imageloader.core.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.InputStream;

import geeksammao.bingyan.net.imageloader.ImageLoader;
import geeksammao.bingyan.net.imageloader.cache.disk.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.disk.MD5;
import geeksammao.bingyan.net.imageloader.cache.memory.MemoryCache;
import geeksammao.bingyan.net.imageloader.core.network.http.HttpUtil;
import geeksammao.bingyan.net.imageloader.core.network.result.RequestResult;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class HttpTask extends LoadTask {
    public HttpTask(ImageLoader imageLoader, String uri, Handler handler, DiskCache diskCache, MemoryCache memoryCache) {
        super(imageLoader, uri, handler, diskCache, memoryCache);
    }

    @Override
    void doTask() {
        HttpUtil httpUtil = HttpUtil.getInstance();
        try {
            RequestResult<InputStream> result = httpUtil.getInputStream(uri);

            if (result.getStatus() == HttpUtil.HTTP_OK) {
                InputStream inputStream = result.getData();
                byte[] bitmapBytes = convertStreamToByArray(inputStream);

                // save as disk cache
                String cacheFileName = MD5.hashKeyForDisk(uri);
                diskCache.save(cacheFileName, bitmapBytes);

                if (imageView != null && !isCancelled()) {
                    final Bitmap bitmap = ImageUtil.decodeBitmapWithScale(imageView, bitmapBytes);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                    // save as memory cache
                    memoryCache.put(uri, bitmap);
                    return;
                }

                final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                if (!isCancelled()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLoadCompleted(uri, bitmap);
                        }
                    });
                    memoryCache.put(uri, bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (callback != null) {
                callback.onLoadFailed(uri);
            }
        }
    }
}
