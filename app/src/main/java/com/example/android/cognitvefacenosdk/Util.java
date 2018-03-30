package com.example.android.cognitvefacenosdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.example.android.cognitvefacenosdk.faceapi.FaceRectangle;
import com.example.android.cognitvefacenosdk.faceapi.ImageTooSmall;
import com.example.android.cognitvefacenosdk.faceapi.MicrosoftFaceApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    /*
     * The Microsoft API can only handles images in the size range 1Kb to 4Mb.
     * If the image is too small we throw an exception if it's too large we resize it.
     */
    public static Pair<byte[], Float> bitmapToSizeLimitedByteArray(final Bitmap imageBitmap) throws ImageTooSmall {
        byte[] imageBytes = Util.bitmapToBytes(imageBitmap);
        float scale = 1.0f;
        Log.i(TAG, "Bitmap width = " + imageBitmap.getWidth() + ", height = " + imageBitmap.getHeight() + ", bytes = " + imageBytes.length);

        if (imageBytes.length > MicrosoftFaceApi.MAX_IMAGE_BYTES) {
            Bitmap bitmap = imageBitmap;
            do {
                // keep reducing the scale until the image is small enough
                scale = scale - 0.1f;
                Log.d(TAG, "Scaling image by " + scale);
                bitmap = Util.resizeBitmapByScale(bitmap, scale, bitmap != imageBitmap);
                imageBytes = Util.bitmapToBytes(bitmap);
                Log.i(TAG, "Bitmap width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight() + ", bytes = " + imageBytes.length);
            } while ((imageBytes.length > MicrosoftFaceApi.MAX_IMAGE_BYTES) && (scale > 0.0f));
            return Pair.create(imageBytes, scale);

        } else if (imageBytes.length < MicrosoftFaceApi.MIN_IMAGE_BYTES) {
            Log.i(TAG, "Image too small. Size = " + imageBytes.length);
            throw new ImageTooSmall(imageBytes.length);
        }

        return Pair.create(imageBytes, scale);
    }

    public static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    public static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static byte[] bitmapToBytes(Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static FaceRectangle scaleRectangle(FaceRectangle rectangle, float scale) {
        FaceRectangle result = new FaceRectangle();
        final float convertScale = 1.0f / scale;
        result.setHeight(scaleInt(rectangle.getHeight(), convertScale));
        result.setWidth(scaleInt(rectangle.getWidth(), convertScale));
        result.setLeft(scaleInt(rectangle.getLeft(), convertScale));
        result.setTop(scaleInt(rectangle.getTop(), convertScale));
        return result;
    }

    public static int scaleInt(int value, float scale) {
        return Math.round(value * scale);
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

}
