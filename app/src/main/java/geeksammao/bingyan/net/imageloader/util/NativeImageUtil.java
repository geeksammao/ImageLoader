package geeksammao.bingyan.net.imageloader.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class NativeImageUtil {
    public static List<String> getNativeImagePath(Context context) {
        List<String> imagePathList = new ArrayList<>();
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();
        // get only .jpg and .png image file
        // ordered by date
        Cursor mCursor = mContentResolver.query(mImageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
        if (mCursor == null) {
            return null;
        }

        mCursor.moveToLast();
        String firstPath = mCursor.getString(mCursor
                .getColumnIndex(MediaStore.Images.Media.DATA));
        imagePathList.add(firstPath);


        while (mCursor.moveToPrevious()) {
            String path = mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            imagePathList.add(path);
        }
        mCursor.close();

        return imagePathList;
    }
}
