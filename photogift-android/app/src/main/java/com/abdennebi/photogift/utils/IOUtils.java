package com.abdennebi.photogift.utils;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    public static void InputStreamToFile(InputStream initialStream, File targetFile) throws IOException {
        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
    }

    public static String copyContent(Uri uri, ContentResolver resolver) throws IOException{
        InputStream is = null;
        try {
            is = resolver.openInputStream(uri);
            File outputMediaFile = IntentsUtils.getOutputMediaFile(IntentsUtils.MEDIA_TYPE_IMAGE);
            InputStreamToFile(is, outputMediaFile);
            is.close();
            return  outputMediaFile.getPath();
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }
    }
}
