package geeksammao.bingyan.net.imageloader.network.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import geeksammao.bingyan.net.imageloader.ImageLoader;
import geeksammao.bingyan.net.imageloader.R;
import geeksammao.bingyan.net.imageloader.cache.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.MD5;
import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;
import geeksammao.bingyan.net.imageloader.util.MyApplication;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class FileTask extends BaseTask {
    private ImageLoader imageLoader;
    private String uri;
    private String actualUri;
    private Handler handler;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;
    private DiskCache diskCache;

    public FileTask(ImageLoader imageLoader, String uri, Handler handler, DiskCache diskCache, MemoryLRUCache<String, Bitmap> memoryLRUCache) {
        // get the actual path
        this.uri = uri;
        this.imageLoader = imageLoader;
        this.actualUri = uri.substring(5);
        this.handler = handler;
        this.memoryLRUCache = memoryLRUCache;
        this.diskCache = diskCache;
    }

    @Override
    void startTask() {
        if (imageView != null) {
            if (imageView.getTag() != null && uri.equals(imageView.getTag())) {
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
                    memoryLRUCache.put(uri, bitmap);
                }
                Log.e("sam", "tag equals");
            } else {
                Log.e("sam", "tag not equals");
                if (imageView.getTag() != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.color.white, new BitmapFactory.Options()));
                            imageLoader.loadImageToImageView((String) imageView.getTag(), imageView);
                        }
                    });
                }
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
            memoryLRUCache.put(uri, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onLoadFailed(uri);
            }
        }
    }

    private byte[] convertStreamToByArray(BufferedInputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length;
        byte[] bytes = new byte[1024];
        while ((length = inputStream.read(bytes)) != -1) {
            baos.write(bytes, 0, length);
        }

        return baos.toByteArray();
    }
}
