package com.abdennebi.photogift.utils;


import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IntentsUtils {

    private static final String TMP_PHOTO_FILENAME = "photogift.jpg";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 3;

    public class Extra {
        /**
         * The key extra for a Gift Chain Id.
         */
        public final static String GIFT_CHAIN_ID = "GIFT_CHAIN_ID";

        /**
         * To carry the new created Gift.
         */
        public final static String CREATED_GIFT = "CREATED_GIFT";
    }

    /**
     * Create an {@link android.content.Intent} to capture an image from the device camera.
     *
     * @return the camera intent.
     */
    public static Intent getCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoImageUri());
        return intent;
    }

    /**
     * Get the URI of the temporary file used to capture a photo.
     *
     * @return the URI of the temporary file.
     */
    public static Uri getPhotoImageUri() {
        File f = new File(Environment.getExternalStorageDirectory(), TMP_PHOTO_FILENAME);
        return Uri.fromFile(f);
    }

    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public static File getOutputMediaFile(int type) {
        Log.d("MyCameraApp", "getOutputMediaFile() type:" + type);
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        // For future implementation: store videos in a separate directory
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "PhotoGift");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else if (type == MEDIA_TYPE_AUDIO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "AUD_" + timeStamp + ".3gp");
        } else {
            Log.e("MyCameraApp", "typ of media file not supported: type was:" + type);
            return null;
        }

        return mediaFile;
    }
}


