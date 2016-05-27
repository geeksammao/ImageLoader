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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService defaultThreadPool = Executors.newFixedThreadPool(4);
    private ExecutorService  serialThreadPool = Executors.newFixedThreadPool(2);

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
        memoryLRUCache = new MemoryLRUCache<String, Bitmap>(maxSize / 6) {
            @Override
            protected int sizeOf(String s, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public void setPlaceholder(int resID) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        placeholderBitmap = BitmapFactory.decodeResource(res, resID, options);
    }

    public void loadImage(String uri, ImageLoadCallback callback) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()){
            throw new IllegalThreadStateException("Must call this method from main thread");
        }

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
        if (Thread.currentThread() != Looper.getMainLooper().getThread()){
            throw new IllegalThreadStateException("Must call this method from main thread");
        }

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

    private void loadImageFromFile(String uri, final ImageView imageView, ImageLoadCallback callback) {
        Bitmap bitmap = getImageFromCache(uri, imageView);
        if (bitmap != null) {
            if (callback == null) {
                imageView.setImageBitmap(bitmap);
            } else {
                callback.onLoadCompleted(uri, bitmap);
            }
            return;
        }

//        final String cacheFileName = MD5.hashKeyForDisk(uri);
//        final InputStream inputStream = diskCache.getStream(cacheFileName);
//
//        final String url = uri;
//
//        if (inputStream != null) {
//            serialThreadPool.execute(new Runnable() {
//                @Override
//                public void run() {
//                    final Bitmap mBitmap = BitmapFactory.decodeStream(inputStream);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            imageView.setImageBitmap(mBitmap);
//                        }
//                    });
//                    memoryLRUCache.put(url, mBitmap);
//                }
//            });
//
//            return;
//        }

        if (urlMap.containsKey(imageView) && imageView != null) {
            if (uri.equals(urlMap.get(imageView))) {
                return;
            } else {
                imageView.setImageBitmap(placeholderBitmap);
                urlMap.put(imageView,uri);
            }
        } else if (!urlMap.containsKey(imageView) && imageView != null){
            urlMap.put(imageView, uri);
        }

        if (imageView != null) {
            imageView.setTag(uri);
        }

        FileTask task = new FileTask(this,uri, handler, diskCache,memoryLRUCache);
        if (callback != null) {
            // can play some animation or display progress bar here
            callback.onLoadStarted(uri);
            task.setCallback(callback);
        }
        if (imageView != null) {
            task.setImageView(imageView);
        }
        defaultThreadPool.execute(task);
    }

    private void loadImageFromHttp(String uri, final ImageView imageView, final ImageLoadCallback callback) {
        // 1.memory cache
        Bitmap bitmap = getImageFromCache(uri, imageView);

        if (bitmap != null) {
            if (callback == null) {
                imageView.setImageBitmap(bitmap);
            } else {
                callback.onLoadCompleted(uri, bitmap);
            }
            return;
        }

        // 2.disk cache
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

            return;
        }

        // 3.http
        if (urlMap.containsKey(imageView) && imageView != null) {
            if (uri.equals(urlMap.get(imageView))) {
                return;
            } else {
                urlMap.put(imageView,uri);
            }
        } else {
            urlMap.put(imageView, uri);
        }

        if (imageView != null) {
            imageView.setTag(uri);
        }

        HttpTask task = new HttpTask(this, uri, handler, diskCache, memoryLRUCache);
        if (callback != null) {
            // can play some animation or display progress bar here
            callback.onLoadStarted(uri);
            task.setCallback(callback);
        }
        if (imageView != null) {
            task.setImageView(imageView);
        }
        defaultThreadPool.execute(task);
    }

    private void loadImageFromAssets(String uri, ImageView imageView, ImageLoadCallback callback) {
        Bitmap bitmap = getImageFromCache(uri, imageView);
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
        defaultThreadPool.execute(task);
    }

    private Bitmap getImageFromCache(String uri, ImageView imageView) {
        Bitmap bitmap = memoryLRUCache.get(uri);
        if (bitmap == null) {
            imageView.setImageBitmap(placeholderBitmap);
        } else {
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
