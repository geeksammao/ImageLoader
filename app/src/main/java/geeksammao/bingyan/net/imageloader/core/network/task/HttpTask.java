package geeksammao.bingyan.net.imageloader.core.network.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.InputStream;

import geeksammao.bingyan.net.imageloader.ImageLoader;
import geeksammao.bingyan.net.imageloader.cache.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.MD5;
import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.core.network.http.HttpUtil;
import geeksammao.bingyan.net.imageloader.core.network.result.RequestResult;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class HttpTask extends BaseTask {
    private ImageLoader imageLoader;
    private DiskCache diskCache;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;
    private Handler handler;
    private String uri;

    public HttpTask(ImageLoader imageLoader, String uri, Handler handler, DiskCache diskCache, MemoryLRUCache<String, Bitmap> memoryLRUCache) {
        super();
        this.imageLoader = imageLoader;
        this.handler = handler;
        this.diskCache = diskCache;
        this.memoryLRUCache = memoryLRUCache;
        this.uri = uri;
    }

    @Override
    void startTask() {
        HttpUtil httpUtil = HttpUtil.getInstance();
        try {
            RequestResult<InputStream> result = httpUtil.getInputStream(uri);

            if (result.getStatus() == HttpUtil.HTTP_OK) {
                InputStream inputStream = result.getData();
                byte[] bitmapBytes = convertStreamToByArray(inputStream);

                // save as disk cache
                String cacheFileName = MD5.hashKeyForDisk(uri);
                diskCache.save(cacheFileName, bitmapBytes);

                if (imageView != null) {
                    // if the tag equals the url,
                    // set the bitmap to image
                    if (imageView.getTag() != null && uri.equals(imageView.getTag())) {
                        final Bitmap bitmap = ImageUtil.decodeBitmapWithScale(imageView, bitmapBytes);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                        // save as memory cache
                        memoryLRUCache.put(uri, bitmap);
//                        imageLoader.removeUrlFromMap(imageView);
                    }
                    // else,
                    // start a new task
                    else {
//                        HttpTask newTask = new HttpTask(imageLoader, (String) imageView.getTag(),
//                                handler, diskCache, memoryLRUCache);
//                        newTask.setImageView(imageView);
//                        imageLoader.executeNewTask(newTask);
                        if (imageView.getTag() != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageLoader.loadImageToImageView((String) imageView.getTag(), imageView);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageLoader.loadImageToImageView(uri, imageView);
                                }
                            });
                        }
                    }

                    return;
                }

                final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoadCompleted(uri, bitmap);
                    }
                });
                memoryLRUCache.put(uri, bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (callback != null) {
                callback.onLoadFailed(uri);
            }
        }

    }
}
