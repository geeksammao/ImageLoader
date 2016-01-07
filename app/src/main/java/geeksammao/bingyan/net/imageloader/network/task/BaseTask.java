package geeksammao.bingyan.net.imageloader.network.task;

import android.widget.ImageView;

import geeksammao.bingyan.net.imageloader.callback.ImageLoadCallback;

/**
 * Created by Geeksammao on 1/6/16.
 */
public abstract class BaseTask extends Thread {

    @Override
    public void run() {
        super.run();

        startTask();
    }

    abstract void startTask();

    public abstract void setCallback(ImageLoadCallback callback);

    public abstract void setImageView(ImageView imageView);
}
