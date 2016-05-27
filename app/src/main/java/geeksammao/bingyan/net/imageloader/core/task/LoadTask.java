package geeksammao.bingyan.net.imageloader.core.task;

import android.os.Handler;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import geeksammao.bingyan.net.imageloader.ImageLoader;
import geeksammao.bingyan.net.imageloader.cache.disk.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.memory.MemoryCache;
import geeksammao.bingyan.net.imageloader.core.callback.ImageLoadCallback;

/**
 * Created by Geeksammao on 1/6/16.
 */
public abstract class LoadTask implements Runnable {
    protected ImageLoadCallback callback;
    protected ImageView imageView;
    public Future<?> future;
    protected ImageLoader imageLoader;
    protected DiskCache diskCache;
    protected MemoryCache memoryCache;
    protected Handler handler;
    protected String uri;

    public LoadTask(ImageLoader imageLoader, String uri, Handler handler, DiskCache diskCache, MemoryCache memoryCache) {
        this.imageLoader = imageLoader;
        this.handler = handler;
        this.diskCache = diskCache;
        this.memoryCache = memoryCache;
        this.uri = uri;
    }

    @Override
    public void run() {
        startTask();
        if (imageView != null) {
            imageLoader.removeUrlFromMap(imageView);
        }
    }

    abstract void startTask();

    public void setCallback(ImageLoadCallback callback) {
        this.callback = callback;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    byte[] convertStreamToByArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length;
        byte[] bytes = new byte[1024];
        while ((length = inputStream.read(bytes)) != -1) {
            baos.write(bytes, 0, length);
        }
        return baos.toByteArray();
    }

    public boolean cancel() {
        return future != null && future.cancel(false);
    }
}
