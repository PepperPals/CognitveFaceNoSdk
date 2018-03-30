package com.example.android.cognitvefacenosdk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.cognitvefacenosdk.faceapi.Face;
import com.example.android.cognitvefacenosdk.faceapi.FaceApiError;
import com.example.android.cognitvefacenosdk.faceapi.FaceRectangle;
import com.example.android.cognitvefacenosdk.faceapi.MicrosoftFaceApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String MS_API_KEY = "<YOUR API KEY HERE>";

    MicrosoftFaceApi faceService;

    private static final int PICK_IMAGE = 1;

    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 3;

    private ProgressDialog detectionProgressDialog;

    private String mCurrentPhotoPath;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView1);

        Button browseButton = (Button) findViewById(R.id.button1);
        browseButton.setOnClickListener(v -> {
            Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
            gallIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(gallIntent, getString(R.string.select_image)), PICK_IMAGE);
        });

        Button cameraButton = (Button) findViewById(R.id.button2);
        cameraButton.setOnClickListener(v -> {
            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showMessageOKCancel(R.string.camera_access,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[] {Manifest.permission.CAMERA},
                                            REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
            takePhoto();
        });

        faceService = ServiceGenerator.createService(MicrosoftFaceApi.class);

        detectionProgressDialog = new ProgressDialog(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    takePhoto();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Use of camera denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create photo image file");
                e.printStackTrace();
            }

            if (null != photoFile) {
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Log.i(TAG, "Got image browse result");
            if (requestCode == PICK_IMAGE && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageView.setImageBitmap(bitmap);
                    detectAndFrame(bitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to obtain file");
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.i(TAG, "Got image captured using camera");
                Log.d(TAG, "Image location = " + mCurrentPhotoPath);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                imageView.setImageBitmap(bitmap);
                detectAndFrame(bitmap);
            }
        }
    }


    /*
     * Detect faces by uploading face images
     * Frame faces after detection
     */
    private void detectAndFrame(final Bitmap imageBitmap) {
        Log.d(TAG, "detectAndFrame");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), outputStream.toByteArray());

        //publishProgress("Detecting...");
        Call<List<Face>> request = faceService.detectFaces(MS_API_KEY, true, false, null, body);
        Log.d(TAG, "Executing request");
        detectionProgressDialog.show();
        request.enqueue(new Callback<List<Face>>() {
            @Override
            public void onResponse(Call<List<Face>> call, Response<List<Face>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Request succeeded");
                    detectionProgressDialog.dismiss();
                    ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                    imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, response.body()));
                    imageBitmap.recycle();
                } else {
                    Log.d(TAG, "Request failed with code: " + response.code());
                    FaceApiError error = parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);
                    detectionProgressDialog.setMessage("Detection failed");
                }
            }

            @Override
            public void onFailure(Call<List<Face>> call, Throwable t) {
                Log.e(TAG, "Detection failed.", t);
                detectionProgressDialog.setMessage("Detection failed");
            }
        });
    }

    private FaceApiError parseFaceApiError(Response<?> response) {
        Converter<ResponseBody, FaceApiError> converter =
                ServiceGenerator.retrofit
                        .responseBodyConverter(FaceApiError.class, new Annotation[0]);

        FaceApiError error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            Log.e(TAG, "Enable to parse error response", e);
            return new FaceApiError();
        }

        return error;
    }

    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, List<Face> faces) {
        Log.d(TAG, "drawFaceRectanglesOnBitmap");
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            Log.d(TAG, "#faces: " + faces.size());
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.getFaceRectangle();
                canvas.drawRect(
                        faceRectangle.getLeft(),
                        faceRectangle.getTop(),
                        faceRectangle.getRight(),
                        faceRectangle.getBottom(),
                        paint);
            }
        } else {

        }
        return bitmap;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showMessageOKCancel(int stringId, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(stringId)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }
}
