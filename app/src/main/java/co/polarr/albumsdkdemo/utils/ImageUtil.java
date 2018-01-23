package co.polarr.albumsdkdemo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Colin on 2017/11/1.
 */

public class ImageUtil {
    // decode a thumb bitmap for a specific size
    public static Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        try {
            int degree = 0;
            ExifInterface exif;
            try {
                exif = new ExifInterface(path);
            } catch (Exception e) {
                exif = null;
            }
            if (exif != null) {
                // get the degree of image file
                int ori = exif
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                // compute the rotation of file.
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        degree = 0;
                        break;
                }
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            //not a bitmap file
            if (options.outHeight < 0) {
                return null;
            }
            options.inSampleSize = computeScale(options, viewWidth, viewHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);

            return getRotatedImage(bitmap, degree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BitmapFactory.Options decodeImageSize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    public static Bitmap getScaledBitmap(String imgPath, int destWidth, int destHeight) {
        int degree = 0;
        ExifInterface exif;
        try {
            exif = new ExifInterface(imgPath);
        } catch (Exception e) {
            exif = null;
        }
        if (exif != null) {
            // get the degree of image file
            int ori = exif
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            // compute the rotation of file.
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);
        //not a bitmap file
        if (options.outHeight < 0) {
            return null;
        }
        int inSampleSize = computeScale(options, destWidth, destHeight);
        options.inSampleSize = Math.max(1, inSampleSize - 1);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, true);
        if (bitmap != scaledBitmap) {
            bitmap.recycle();
        }
        bitmap = scaledBitmap;

        return getRotatedImage(scaledBitmap, degree);
    }

    public static Bitmap getScaledFitBitmap(String imgPath, int destWidth, int destHeight) {
        int degree = 0;
        ExifInterface exif;
        try {
            exif = new ExifInterface(imgPath);
        } catch (Exception e) {
            exif = null;
        }
        if (exif != null) {
            // get the degree of image file
            int ori = exif
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            // compute the rotation of file.
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);
        //not a bitmap file
        if (options.outHeight < 0) {
            return null;
        }
        int inSampleSize = computeScale(options, destWidth, destHeight);
        options.inSampleSize = Math.max(1, inSampleSize - 1);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
        float minScale = Math.min((float) destWidth / bitmap.getWidth(), (float) destHeight / bitmap.getHeight());
        minScale = Math.min(1, minScale);
        int targetWidth = (int) (bitmap.getWidth() * minScale);
        int targetHeight = (int) (bitmap.getHeight() * minScale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        if (bitmap != scaledBitmap) {
            bitmap.recycle();
        }

        return getRotatedImage(scaledBitmap, degree);
    }

    // get the right rotation bitmap
    private static Bitmap getRotatedImage(Bitmap bitmap, int degrees) {
        if (bitmap != null && degrees != 0) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix m = new Matrix();
            m.postRotate(degrees, w / 2, h / 2);
            Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
            if (bm != bitmap) {
                bitmap.recycle();
            }
            bitmap = bm;
        }

        return bitmap;
    }

    // compute the sample size for a specific size
    private static int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        float inSampleSize = 1;
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        int maxViewSize = Math.max(viewHeight, viewWidth);

        if (bitmapWidth > maxViewSize || bitmapHeight > maxViewSize) {
            float widthScale = (float) bitmapWidth / (float) maxViewSize;
            float heightScale = (float) bitmapHeight / (float) maxViewSize;
            inSampleSize = Math.max(widthScale, heightScale);
            inSampleSize = (float) Math.max(Math.ceil(inSampleSize), 1);
        }
        return (int) inSampleSize;
    }

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);

    public static long getPhotoCreationTime(File photo) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(photo.getAbsolutePath());
            String date = exif.getAttribute(ExifInterface.TAG_DATETIME);

            try {
                return simpleDateFormat.parse(date).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
