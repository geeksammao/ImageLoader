package geeksammao.bingyan.net.imageloader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import geeksammao.bingyan.net.imageloader.cache.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.MD5;
import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.core.callback.ImageLoadCallback;
import geeksammao.bingyan.net.imageloader.core.network.task.AssetsTask;
import geeksammao.bingyan.net.imageloader.core.network.task.BaseTask;
import geeksammao.bingyan.net.imageloader.core.network.task.FileTask;
import geeksammao.bingyan.net.imageloader.core.network.task.HttpTask;
import geeksammao.bingyan.net.imageloader.util.MyApplication;

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
    private Resources res;
    private HashMap<ImageView, String> urlMap;
    private Bitmap placeholderBitmap;
    // the UI thread handler
    private Handler handler = new Handler(Looper.getMainLooper());
    private DiskCache diskCache;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;
    private BaseTask loadTask;
    private ExecutorService defaultThreadPool = Executors.newFixedThreadPool(4);
    private ExecutorService serialThreadPool = Executors.newFixedThreadPool(2);

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
        urlMap = new HashMap<>();

        diskCache = DiskCache.getInstance(context, CACHE_DIR_NAME);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        res = context.getResources();
        placeholderBitmap = BitmapFactory.decodeResource(res, R.color.white, options);
        memoryLRUCache = new MemoryLRUCache<>(maxSize / 6);
    }

    public void setPlaceholder(int resID) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        placeholderBitmap = BitmapFactory.decodeResource(res, resID, options);
    }

    public void loadImage(String uri, ImageLoadCallback callback) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalThreadStateException("Must call this method from main thread");
        }

        // load from memory cache
        if (loadFromMemoryCache(uri, null, callback)) {
            return;
        }

        // get the scheme by substring the uri
        String scheme = TextUtils.substring(uri, 0, 4);

        switch (scheme) {
            case HTTP:
                loadTask = new HttpTask(this, uri, handler, diskCache, memoryLRUCache);
                break;
            case FILE:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryLRUCache);
                break;
            case CONTENT_PROVIDER:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryLRUCache);
                break;
            case ASSETS:
                loadTask = new AssetsTask(uri, handler, memoryLRUCache, MyApplication.getInstance());
                break;
            default:
                throw new IllegalArgumentException("Unknown image uri");
        }
        callback.onLoadStarted(uri);
        loadTask.setCallback(callback);

        defaultThreadPool.execute(loadTask);
    }

    public void loadImageToImageView(String uri, ImageView view) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalThreadStateException("Must call this method from main thread");
        }

        // load from memory cache
        if (loadFromMemoryCache(uri, view, null)) {
            return;
        }

        // load from disk cache
        if (loadFromDiskCache(uri, view)) {
            return;
        }

        String scheme = TextUtils.substring(uri, 0, 4);
        switch (scheme) {
            case HTTP:
                loadTask = new HttpTask(this, uri, handler, diskCache, memoryLRUCache);
                break;
            case FILE:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryLRUCache);
                break;
            case CONTENT_PROVIDER:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryLRUCache);
                break;
            case ASSETS:
                loadTask = new AssetsTask(uri, handler, memoryLRUCache, MyApplication.getInstance());
                break;
            default:
                throw new IllegalArgumentException("Illegal image uri");
        }
        loadTask.setImageView(view);

        defaultThreadPool.execute(loadTask);
    }

    private boolean loadFromDiskCache(String uri, final ImageView imageView) {
        final String cacheFileName = MD5.hashKeyForDisk(uri);
        final InputStream inputStream = diskCache.getStream(cacheFileName);

        final String url = uri;

        if (inputStream != null) {
            serialThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    final Bitmap mBitmap = BitmapFactory.decodeStream(inputStream);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(mBitmap);
                        }
                    });
                    memoryLRUCache.put(url, mBitmap);
                }
            });

            return true;
        }
        return false;
    }

    private boolean loadFromMemoryCache(String uri, ImageView imageView, ImageLoadCallback callback) {
        if (imageView == null) {
            return false;
        }

        Bitmap bitmap = getImageFromCache(uri, imageView);
        if (bitmap != null) {
            if (callback == null) {
                imageView.setImageBitmap(bitmap);
            } else {
                callback.onLoadCompleted(uri, bitmap);
            }
            return true;
        }
        return false;
    }

    private Bitmap getImageFromCache(String uri, ImageView imageView) {
        Bitmap bitmap = memoryLRUCache.get(uri);
        if (bitmap == null) {
            Log.e("a", "cache not hit");
            imageView.setImageBitmap(placeholderBitmap);
        } else {
            Log.e("a", "cache hit");
        }

        return bitmap;
    }

    public void removeUrlFromMap(ImageView imageView) {
        urlMap.remove(imageView);
    }

    public void executeNewTask(BaseTask task) {
        defaultThreadPool.execute(task);
    }

    public void cancelAll() {
        defaultThreadPool.shutdown();
    }
}
