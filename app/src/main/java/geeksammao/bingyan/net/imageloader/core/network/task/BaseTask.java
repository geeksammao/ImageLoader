package geeksammao.bingyan.net.imageloader.core.network.task;

import android.widget.ImageView;

import geeksammao.bingyan.net.imageloader.core.callback.ImageLoadCallback;

/**
 * Created by Geeksammao on 1/6/16.
 */
public abstract class BaseTask implements Runnable {
    protected ImageLoadCallback callback;
    protected ImageView imageView;

    @Override
    public void run() {
        startTask();
    }

    abstract void startTask();

    public void setCallback(ImageLoadCallback callback) {
        this.callback = callback;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

}
