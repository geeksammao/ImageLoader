package geeksammao.bingyan.net.imageloader.core.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import geeksammao.bingyan.net.imageloader.ImageLoader;
import geeksammao.bingyan.net.imageloader.cache.disk.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.disk.MD5;
import geeksammao.bingyan.net.imageloader.cache.memory.MemoryCache;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class FileTask extends LoadTask {
    private String actualUri;

    public FileTask(ImageLoader imageLoader, String uri, Handler handler, DiskCache diskCache, MemoryCache memoryCache) {
        super(imageLoader, uri, handler, diskCache, memoryCache);
        actualUri = uri.substring(5);
    }

    @Override
    void startTask() {
        if (imageView != null) {
            File file = new File(actualUri);
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file), 1024);
                byte[] bitmapBytes = convertStreamToByArray(bufferedInputStream);
                // save as disk cache
                String cacheFileName = MD5.hashKeyForDisk(uri);
                diskCache.save(cacheFileName, bitmapBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final Bitmap bitmap = ImageUtil.decodeBitmapWithScale(imageView, actualUri);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
            if (bitmap != null) {
                memoryCache.put(uri, bitmap);
            }
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
            memoryCache.put(uri, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onLoadFailed(uri);
            }
        }
    }
}
