package geeksammao.bingyan.net.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import geeksammao.bingyan.net.imageloader.cache.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.MD5;
import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.callback.ImageLoadCallback;
import geeksammao.bingyan.net.imageloader.network.task.AssetsTask;
import geeksammao.bingyan.net.imageloader.network.task.FileTask;
import geeksammao.bingyan.net.imageloader.network.task.HttpTask;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class ImageLoader {
    public static final String HTTP = "http";
    public static final String FILE = "file";
    public static final String CONTENT_PROVIDER = "cont";
    public static final String ASSETS = "asse";
    public static final String CACHE_DIR_NAME = "image_cache";

    private static volatile ImageLoader mImageLoader;
    // the UI thread handler
    private Handler handler = new Handler(Looper.getMainLooper());
    private DiskCache diskCache;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

    public static ImageLoader getInstance(Context context) {
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(context);
                }
            }
        }

        return mImageLoader;
    }

    private ImageLoader(Context context) {
        int maxSize = (int) Runtime.getRuntime().maxMemory();

        diskCache = DiskCache.getInstance(context, CACHE_DIR_NAME);
        memoryLRUCache = new MemoryLRUCache<String, Bitmap>(maxSize / 10) {
            @Override
            protected int sizeOf(String s, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public void loadImage(String uri, ImageLoadCallback callback) {
        // get the scheme by substring the uri
        String scheme = TextUtils.substring(uri, 0, 4);

        switch (scheme) {
            case HTTP:
                loadImageFromHttp(uri, null, callback);
                break;
            case FILE:
                loadImageFromFile(uri, null, callback);
                break;
            case CONTENT_PROVIDER:
                loadImageFromFile(uri, null, callback);
                break;
            case ASSETS:
                loadImageFromAssets(uri, null, callback);
                break;
            default:
                throw new IllegalArgumentException("Unknown image uri");
        }
    }

    public void loadImageToImageView(String uri, ImageView view) {
        String scheme = TextUtils.substring(uri, 0, 4);

        switch (scheme) {
            case HTTP:
                loadImageFromHttp(uri, view, null);
                break;
            case FILE:
                loadImageFromFile(uri, view, null);
                break;
            case CONTENT_PROVIDER:
                loadImageFromFile(uri, view, null);
                break;
            case ASSETS:
                loadImageFromAssets(uri, view, null);
                break;
            default:
                throw new IllegalArgumentException("Illegal image uri");
        }
    }

    private void loadImageFromFile(String uri, ImageView imageView, ImageLoadCallback callback) {
        Bitmap bitmap = getImageFromCache(uri);
        if (bitmap != null) {
            if (callback == null) {
                imageView.setImageBitmap(bitmap);
            } else {
                callback.onLoadCompleted(uri, bitmap);
            }
            return;
        }

        FileTask task = new FileTask(uri, handler, memoryLRUCache);
        if (callback != null) {
            // can play some animation or display progress bar here
            callback.onLoadStarted(uri);
            task.setCallback(callback);
        }
        if (imageView != null) {
            task.setImageView(imageView);
        }
        threadPool.execute(task);
    }

    private void loadImageFromHttp(final String uri, ImageView imageView, final ImageLoadCallback callback) {
        Bitmap bitmap = getImageFromCache(uri);
        if (bitmap != null) {
            if (callback == null) {
                imageView.setImageBitmap(bitmap);
            } else {
                callback.onLoadCompleted(uri, bitmap);
            }
            return;
        }

        HttpTask task = new HttpTask(uri, handler, diskCache, memoryLRUCache);
        if (callback != null) {
            // can play some animation or display progress bar here
            callback.onLoadStarted(uri);
            task.setCallback(callback);
        }
        if (imageView != null) {
            task.setImageView(imageView);
        }
        threadPool.execute(task);
    }

    private void loadImageFromAssets(String uri, ImageView imageView, ImageLoadCallback callback) {
        Bitmap bitmap = getImageFromCache(uri);
        if (bitmap != null) {
            if (callback == null) {
                imageView.setImageBitmap(bitmap);
            } else {
                callback.onLoadCompleted(uri, bitmap);
            }
            return;
        }

        AssetsTask task = new AssetsTask(uri, handler, memoryLRUCache, MyApplication.getInstance());
        if (callback != null) {
            // can play some animation or display progress bar here
            callback.onLoadStarted(uri);
            task.setCallback(callback);
        }
        if (imageView != null) {
            task.setImageView(imageView);
        }
        threadPool.execute(task);
    }

    private Bitmap getImageFromCache(String uri) {
        Bitmap bitmap = memoryLRUCache.get(uri);
        if (bitmap == null) {
            String cacheFileName = MD5.hashKeyForDisk(uri);
            bitmap = BitmapFactory.decodeStream(diskCache.getStream(cacheFileName));
        }

        return bitmap;
    }
}
