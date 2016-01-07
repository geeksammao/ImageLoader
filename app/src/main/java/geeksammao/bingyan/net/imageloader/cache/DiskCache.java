package geeksammao.bingyan.net.imageloader.cache;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class DiskCache {
    private final byte[] LOCK_WRITE = new byte[0];
    private static final byte[] LOCK_INIT = new byte[0];
    private final byte[] LOCK_DELETE = new byte[0];

    private static DiskCache diskCache;
    private Context context;

    private File root;

    private DiskCache(Context context, String cacheDirName) {
        this.context = context;
        root = getCacheDir(cacheDirName);
        root.mkdir();
    }

    public static DiskCache getInstance(Context context, String cacheDirName) {
        if (diskCache == null) {
            synchronized (LOCK_INIT) {
                if (diskCache == null) {
                    diskCache = new DiskCache(context, cacheDirName);
                }
            }
        }
        return diskCache;
    }

    public void save(String cacheFileName, byte[] bytes) {
        synchronized (LOCK_WRITE) {
            File cacheFile = new File(root, cacheFileName);

            try {
                if (cacheFile.exists()) {
                    delete(cacheFile);
                }
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));

                bos.write(bytes, 0, bytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public File getCacheDir(String cacheDirName) {
        File cacheDir;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cacheDir = new File(context.getExternalCacheDir(), cacheDirName);
        } else {
            cacheDir = new File(context.getCacheDir(), cacheDirName);
        }

        return cacheDir;
    }


    public InputStream getStream(String cacheFileName) {
        File file = new File(root, cacheFileName);
        BufferedInputStream bis = null;
        FileInputStream fis;

        try {
            if (!file.exists()) {
                return null;
            }
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bis;
    }


    public void delete(File file) {
        synchronized (LOCK_DELETE) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

}
