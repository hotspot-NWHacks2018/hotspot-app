package ca.prieto.hotspot.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Jessica on 2018-01-13.
 */

public class ImageUtils {
    public static String imageToBase64(Bitmap image){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // put image into the output stream
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap base64ToImage(String base64){
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        return BitmapFactory.decodeStream(is);
    }
}
