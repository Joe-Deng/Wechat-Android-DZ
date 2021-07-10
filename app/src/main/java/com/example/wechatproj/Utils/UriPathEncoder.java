package com.example.wechatproj.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

public class UriPathEncoder {
    private static String TAG = "PathTools";
    public static String getPath(Context context, Uri uri) {
        if(null == uri || context == null){
            return null;
        }
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            Log.d(TAG, "DocumentsContract.isDocumentUri");
            if (isExternalStorageDocument(uri)) {
                Log.d(TAG, "isExternalStorageDocument ");
                return getExternalStroageDocumentPath(uri);
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                Log.d(TAG, "isDownloadsDocument");
                return getDownloadsDocumentPath(context, uri);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                Log.d(TAG, "isMediaDocument");
                return getMediaDocumentPath(context, uri);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            Log.d(TAG, "content equals uri");
            if (isGooglePhotosUri(uri)){
                Log.d(TAG, "isGooglePhotosUri");
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Log.d(TAG, "file equals uri");
            return uri.getPath();
        }
        return uri.getPath();
    }

    private static String getDownloadsDocumentPath(Context context, Uri uri) {
        String id = DocumentsContract.getDocumentId(uri);
        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
        Log.d(TAG, "getDownloadsDocumentPath");
        return getDataColumn(context, contentUri, null, null);
    }

    private static String getMediaDocumentPath(Context context, Uri uri) {
        Log.d(TAG, "getMediaDocumentPath ");
        String docId = DocumentsContract.getDocumentId(uri);
        if(null == docId ){
            Log.d(TAG, "no docId");
            return null;
        }
        String[] split = docId.split(":");
        if(null == split || split.length<2){
            Log.d(TAG, "split failure");
            return null;
        }
        String type = split[0];
        Uri contentUri = null;
        if ("image".equals(type)) {
            Log.d(TAG, "type---image");
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            Log.d(TAG, "type---vedio");
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            Log.d(TAG, "type---audio");
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        String selection = "_id=?";
        String[] selectionArgs = new String[]{split[1]};
        return getDataColumn(context, contentUri, selection, selectionArgs);
    }

    private static String getExternalStroageDocumentPath(Uri uri) {
        Log.d(TAG, "getExternalStroageDocumentPath");
        String docId = DocumentsContract.getDocumentId(uri);
        if(null == docId){
            Log.d(TAG, "no docId");
            return null;
        }
        String[] split = docId.split(":");
        if(null == split || split.length<2){
            Log.d(TAG, "split failure");
            return null;
        }
        String type = split[0];
        if ("primary".equalsIgnoreCase(type)) {
            //内置存储
            Log.d(TAG, "type---primary");
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + split[1];
        } else {
            //外置sd卡
            Log.d(TAG, "type---SDcard");
            String path = System.getenv("SECONDARY_STORAGE") + "/" + split[1];
            return path;
        }
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
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "getDataColumn");
        String column = "_data";
        String[] projection = {column};
        Log.d(TAG, "uri:"+uri.getPath());
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        Log.d(TAG, "No found a path from uri");
        return null;
    }


    /**
     * @param uri The Uri to check.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        Log.d(TAG, "isExternalStorageDocument");
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        Log.d(TAG, "isDownloadsDocument");
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     */
    private static boolean isMediaDocument(Uri uri) {
        Log.d(TAG, "isMediaDocument");
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        Log.d(TAG, "isGooglePhotosUri");
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
