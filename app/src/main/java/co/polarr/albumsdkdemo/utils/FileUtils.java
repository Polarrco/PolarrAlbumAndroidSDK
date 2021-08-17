package co.polarr.albumsdkdemo.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.annotation.RawRes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.List;


/**
 * Created by dinqe on 09/04/16.
 */
public class FileUtils {

    public static void moveFile(File src, File dst) throws IOException {
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(dst).getChannel();
            inputChannel = new FileInputStream(src).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            src.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void copyFileOrPath(File sourceFile, File destFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            return;
        }

        if (sourceFile.isDirectory()) {
            if (destFile.exists() && !destFile.isDirectory()) {
                destFile.delete();
            }
            if (!destFile.exists()) {
                destFile.mkdir();
            }

            for (File file : sourceFile.listFiles()) {
                copyFileOrPath(file, new File(destFile.getPath() + "/" + file.getName()));
            }
        } else {
            copyFile(sourceFile, destFile);
        }
    }

    public static byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        byte[] data = byteBuffer.toByteArray();

        byteBuffer.close();

        return data;
    }

    public static byte[] readBytesFromFile(File file) throws IOException {
        return readBytesFromStream(new FileInputStream(file));
    }

    public static boolean writeBytesToFile(File file, byte[] bytes) throws IOException {
        if (file == null || bytes == null) {
            return false;
        }

        OutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes);

        return true;
    }

    public static Uri getDownloadableMediaUri(Context context, Uri uri) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            return writeToTempImageAndGetPathUri(context, bmp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     * @author boruiwang
     */
    public static boolean isGoogleDriveDocument(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    // this only works for Marshmallow, split[0] is the id of the sd card (random)
                    return "/storage/" + type + "/" + split[1];
                }
            }
            // DownloadsProvider
            if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            } else if (isGoogleDriveDocument(uri)) {
                return getPath(context, getDownloadableMediaUri(context, uri));
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            String path = getDataColumn(context, uri, null, null);
            if (path == null) {
                // this is likely to be google photos uri, but the uri is changing too frequently
                // so we're using null to check against it
                return getPath(context, getDownloadableMediaUri(context, uri));
            } else {
                return path;
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * get all file in a path with extensions
     *
     * @param files    file list to store
     * @param filePath directory path
     * @param exts     extensions
     * @param deep     max deep for search
     */
    public static void getFilesByExts(List<String> files, String filePath, String[] exts, int deep) {
        if (deep == 0) {
            return;
        }
        File f = new File(filePath);
        if (!f.exists() || !f.isDirectory()) {
            return;
        }

        File[] subFiles = f.listFiles();
        for (File subFile : subFiles) {
            for (String ext : exts) {
                if (subFile.isFile() && subFile.getName().toLowerCase().endsWith("." + ext.toLowerCase())) {
                    files.add(subFile.getAbsolutePath());
                } else if (subFile.isDirectory()) {
                    getFilesByExts(files, subFile.getAbsolutePath(), exts, deep - 1);
                }
            }
        }
    }

    /**
     * Copy raw file to target path
     */
    public static final void copyFileFromRawToOthers(final Context context, @RawRes int id, final String targetPath) {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * file comparator by modify date
     */
    public static class FileDateComparator implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            int compareResult = 0;
            File fileLHS = new File(lhs);
            File fileRHS = new File(rhs);

            if (fileLHS.lastModified() < fileRHS.lastModified()) {
                compareResult = 1;
            } else if (fileLHS.lastModified() > fileRHS.lastModified()) {
                compareResult = -1;
            }

            return compareResult;
        }
    }
}



