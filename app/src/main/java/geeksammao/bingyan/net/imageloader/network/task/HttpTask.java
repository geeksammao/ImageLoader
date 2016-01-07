package geeksammao.bingyan.net.imageloader.network.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import geeksammao.bingyan.net.imageloader.cache.DiskCache;
import geeksammao.bingyan.net.imageloader.cache.MD5;
import geeksammao.bingyan.net.imageloader.cache.MemoryLRUCache;
import geeksammao.bingyan.net.imageloader.callback.ImageLoadCallback;
import geeksammao.bingyan.net.imageloader.network.http.HttpUtil;
import geeksammao.bingyan.net.imageloader.network.result.RequestResult;
import geeksammao.bingyan.net.imageloader.util.ImageUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class HttpTask extends BaseTask {
    private DiskCache diskCache;
    private MemoryLRUCache<String, Bitmap> memoryLRUCache;
    private Handler handler;
    private String uri;
    private ImageView imageView;
    private ImageLoadCallback callback;

    public HttpTask(String uri, Handler handler, DiskCache diskCache, MemoryLRUCache<String, Bitmap> memoryLRUCache) {
        super();
        this.handler = handler;
        this.diskCache = diskCache;
        this.memoryLRUCache = memoryLRUCache;
        this.uri = uri;
    }

    @Override
    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setPlaceHolder(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    @Override
    public void setCallback(ImageLoadCallback callback) {
        this.callback = callback;
    }

    @Override
    void startTask() {
        HttpUtil httpUtil = HttpUtil.getInstance();

        try {
            RequestResult<InputStream> result = httpUtil.getInputStream(uri);
            if (result.getStatus() == HttpUtil.HTTP_OK) {
                byte[] bitmapBytes = convertStreamToByArray(result);

                // save as disk cache
                String cacheFileName = MD5.hashKeyForDisk(uri);
                diskCache.save(cacheFileName, bitmapBytes);

                if (imageView != null) {
                    final Bitmap bitmap = ImageUtil.decodeBitmapWithScale(imageView, bitmapBytes);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                    memoryLRUCache.put(uri, bitmap);

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

            if (callback != null){
                callback.onLoadFailed(uri);
            }
        }
    }

    private byte[] convertStreamToByArray(RequestResult<InputStream> result) throws IOException {
        Request request = new Request.Builder()
                .url(uri)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response =  client.newCall(request).execute();

        InputStream inputStream = response.body().byteStream();

        BufferedInputStream buff = new BufferedInputStream(inputStream,1024);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length;
        byte[] bytes = new byte[1024];
        while ((length = buff.read(bytes)) != -1) {
            baos.write(bytes, 0, length);
        }

        return baos.toByteArray();
    }
}
