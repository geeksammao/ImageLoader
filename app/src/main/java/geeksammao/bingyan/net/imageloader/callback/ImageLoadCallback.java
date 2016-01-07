package geeksammao.bingyan.net.imageloader.callback;

import android.graphics.Bitmap;

/**
 * Created by Geeksammao on 1/7/16.
 */
public interface ImageLoadCallback {
    void onLoadStarted(String uri);

    void onLoadCompleted(String uri, Bitmap loadedImage);

    void onLoadFailed(String uri);

    void onLoadCancelled(String uri);
}
