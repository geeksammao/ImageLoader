package geeksammao.bingyan.net.imageloader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import geeksammao.bingyan.net.imageloader.cache.disk.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.disk.MD5;
import geeksammao.bingyan.net.imageloader.cache.memory.MemoryCache;
import geeksammao.bingyan.net.imageloader.cache.memory.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.core.callback.ImageLoadCallback;
import geeksammao.bingyan.net.imageloader.core.task.AssetsTask;
import geeksammao.bingyan.net.imageloader.core.task.FileTask;
import geeksammao.bingyan.net.imageloader.core.task.HttpTask;
import geeksammao.bingyan.net.imageloader.core.task.LoadTask;

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
    private WeakHashMap<ImageView, String> urlMap;
    private Map<String, LoadTask> taskMap;
    public Bitmap placeholderBitmap;
    // the UI thread handler
    private Handler handler = new Handler(Looper.getMainLooper());
    private DiskCache diskCache;
    private MemoryCache memoryCache;
    private LoadTask loadTask;
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
        urlMap = new WeakHashMap<>();
        taskMap = new HashMap<>();

        diskCache = DiskCache.getInstance(context, CACHE_DIR_NAME);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        res = context.getResources();
        placeholderBitmap = BitmapFactory.decodeResource(res, R.color.white, options);
        memoryCache = new MemoryLRUCache<>(maxSize / 6);
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
                loadTask = new HttpTask(this, uri, handler, diskCache, memoryCache);
                break;
            case FILE:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryCache);
                break;
            case CONTENT_PROVIDER:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryCache);
                break;
            case ASSETS:
                loadTask = new AssetsTask(this, uri, handler, diskCache, memoryCache);
                ((AssetsTask)loadTask).setContext(MyApplication.getInstance());
                break;
            default:
                throw new IllegalArgumentException("Unknown image uri");
        }
        callback.onLoadStarted(uri);
        loadTask.setCallback(callback);
        loadTask.future = defaultThreadPool.submit(loadTask);

        taskMap.put(uri, loadTask);
    }

    public void loadImageToImageView(String uri, ImageView view) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalThreadStateException("Must call this method from main thread");
        }

        if (view == null) {
            throw new IllegalArgumentException("The target view cannot be null");
        }
        // load from memory cache and disk cache
        if (loadFromMemoryCache(uri, view, null) || loadFromDiskCache(uri,view)) {
            return;
        }

        // deal with async load image order mess
        String existingUri = urlMap.get(view);
        if (!uri.equals(existingUri)) {
            cancelExistingTask(existingUri,view);
            addUriToMap(uri, view);
        } else {
            return;
        }

        String scheme = TextUtils.substring(uri, 0, 4);
        switch (scheme) {
            case HTTP:
                loadTask = new HttpTask(this, uri, handler, diskCache, memoryCache);
                break;
            case FILE:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryCache);
                break;
            case CONTENT_PROVIDER:
                loadTask = new FileTask(this, uri, handler, diskCache, memoryCache);
                break;
            case ASSETS:
                loadTask = new AssetsTask(this, uri, handler, diskCache, memoryCache);
                ((AssetsTask)loadTask).setContext(MyApplication.getInstance());
                break;
            default:
                throw new IllegalArgumentException("Illegal image uri");
        }
        loadTask.setImageView(view);
        loadTask.future = defaultThreadPool.submit(loadTask);

        taskMap.put(uri, loadTask);
    }

    private void addUriToMap(String uri, ImageView view) {
        if (urlMap != null)
            urlMap.put(view, uri);
        view.setImageBitmap(placeholderBitmap);
    }

    private void cancelExistingTask(String uri,ImageView imageView) {
        LoadTask existingTask = taskMap.get(uri);
        if (existingTask != null && existingTask.cancel()) {
            taskMap.remove(uri);
            imageView.setImageBitmap(placeholderBitmap);
        }
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
                    memoryCache.put(url, mBitmap);
                }
            });

            return true;
        }
        return false;
    }

    private boolean loadFromMemoryCache(String uri, ImageView imageView, ImageLoadCallback callback) {
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
        Bitmap bitmap = memoryCache.get(uri);
        if (bitmap == null && imageView != null) {
            imageView.setImageBitmap(placeholderBitmap);
        }
        return bitmap;
    }

    public void removeUrlFromMap(ImageView imageView) {
        if (imageView != null)
            urlMap.remove(imageView);
    }

    public void removeRunningTask(String uri){
        taskMap.remove(uri);
    }

    public void setMemoryCache(MemoryCache cache){
        this.memoryCache = cache;
    }
    public void executeNewTask(LoadTask task) {
        defaultThreadPool.execute(task);
    }

    public void cancelAll() {
        defaultThreadPool.shutdown();
    }
}
