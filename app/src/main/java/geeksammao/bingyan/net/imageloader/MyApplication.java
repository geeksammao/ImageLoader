package geeksammao.bingyan.net.imageloader;

import android.app.Application;

/**
 * Created by Geeksammao on 1/8/16.
 */
public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}