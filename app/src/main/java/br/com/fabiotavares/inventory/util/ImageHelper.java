package br.com.fabiotavares.inventory.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class ImageHelper {

    public static final byte[] convertBitmapToBlob(Bitmap bitmapImage) {
        // result
        byte[] byteArray;

        try {
            // http://stackoverflow.com/questions/10618325/how-to-create-a-blob-from-bitmap-in-android-activity
            // from user grattmandu03
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream);
            byteArray = byteOutputStream.toByteArray();
        } catch (Exception e) {
            Log.e("EditorActivity", "convertBitmapToBlob exception: " + e);
            return null;
        }

        return byteArray;
    }

    public static final Bitmap convertBlobToBitmap(byte[] imageByteArray) {
        if (imageByteArray == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
    }

}
