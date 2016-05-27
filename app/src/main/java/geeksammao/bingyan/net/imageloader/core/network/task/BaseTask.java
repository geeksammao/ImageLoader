package geeksammao.bingyan.net.imageloader.core.network.task;

import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    byte[] convertStreamToByArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length;
        byte[] bytes = new byte[1024];
        while ((length = inputStream.read(bytes)) != -1) {
            baos.write(bytes, 0, length);
        }

        return baos.toByteArray();
    }
}
