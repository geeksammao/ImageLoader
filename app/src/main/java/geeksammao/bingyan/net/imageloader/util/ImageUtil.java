package geeksammao.bingyan.net.imageloader.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by Geeksammao on 1/7/16.
 */
public class ImageUtil {
    public static void measureImageView(final ImageView imageView, final Point point) {
        imageView.post(new Runnable() {
            @Override
            public void run() {
                int width = imageView.getMeasuredWidth();
                int height = imageView.getMeasuredHeight();

                point.set(width, height);
            }
        });
    }

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

    public static Bitmap decodeBitmapWithScale(ImageView imageView, byte[] bitmapBytes) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);

        Point point = new Point();
        ImageUtil.measureImageView(imageView, point);
        options.inSampleSize = ImageUtil.getScaleSize(options, point.x, point.y);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);
    }

    public static Bitmap decodeBitmapWithScale(ImageView imageView, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        Point point = new Point();
        ImageUtil.measureImageView(imageView, point);
        options.inSampleSize = ImageUtil.getScaleSize(options, point.x, point.y);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeBitmapWithScale(ImageView imageView, InputStream inputStream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);

        Point point = new Point();
        ImageUtil.measureImageView(imageView, point);
        options.inSampleSize = ImageUtil.getScaleSize(options, point.x, point.y);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(inputStream, null, options);
    }
}
