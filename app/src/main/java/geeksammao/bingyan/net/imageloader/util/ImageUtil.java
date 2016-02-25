package geeksammao.bingyan.net.imageloader.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class ImageUtil {
    public static int getScaleSize(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        // 0 means no scaling
        if (viewWidth == 0 || viewHeight == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        // get the scale size according to the view size
        if (bitmapWidth > viewWidth || bitmapHeight > viewHeight) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewHeight);
            // use the smaller scale size
            // to avoid over-scaling
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }

    public static Bitmap decodeBitmapWithScale(final ImageView imageView, byte[] bitmapBytes) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);


        options.inSampleSize = getSampleSize(imageView, options);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);
    }

    private static int getSampleSize(final ImageView imageView, final BitmapFactory.Options options) {
        int width = imageView.getLayoutParams().width;
        int height = imageView.getLayoutParams().height;

        if (width < 0 || height < 0) {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
            imageView.measure(widthMeasureSpec, heightMeasureSpec);

            width = imageView.getMeasuredWidth();
            height = imageView.getMeasuredHeight();
        }

        return getScaleSize(options, width, height);
    }

    public static Bitmap decodeBitmapWithScale(ImageView imageView, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = getSampleSize(imageView, options);
        Log.e("a","sample size" + options.inSampleSize);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeBitmapWithScale(final ImageView imageView, InputStream inputStream) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);


        options.inSampleSize = getSampleSize(imageView, options);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    public static Bitmap decodeBitmapWithScale(final ImageView imageView, Resources res, int id) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, id, options);

        options.inSampleSize = getSampleSize(imageView, options);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, id, options);
    }
}
