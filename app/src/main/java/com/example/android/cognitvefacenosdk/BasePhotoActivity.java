package com.example.android.cognitvefacenosdk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.cognitvefacenosdk.faceapi.Face;
import com.example.android.cognitvefacenosdk.faceapi.FaceApiError;
import com.example.android.cognitvefacenosdk.faceapi.ImageTooSmall;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BasePhotoActivity extends AppCompatActivity {
    private static final String TAG = BasePhotoActivity.class.getSimpleName();

    protected static final int PICK_IMAGE = 1;

    protected static final int REQUEST_IMAGE_CAPTURE = 2;

    protected static final int REQUEST_CODE_ASK_PERMISSIONS = 3;

    protected ProgressDialog detectionProgressDialog;

    protected ImageView imageView;

    protected String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void startSelectPhoto() {
        Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
        gallIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(gallIntent, getString(R.string.select_image)), PICK_IMAGE);
    }

    protected void startTakePhoto() {
        int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                UiUtil.showMessageOKCancel(BasePhotoActivity.this, R.string.camera_access,
                        (dialog, which) -> requestPermissions(new String[]{Manifest.permission.CAMERA},
                                REQUEST_CODE_ASK_PERMISSIONS));
                return;
            }

            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        doTakePhoto();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    doTakePhoto();
                } else {
                    // Permission Denied
                    Toast.makeText(BasePhotoActivity.this, R.string.camera_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void doTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Util.createImageFile(BasePhotoActivity.this);
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create photo image file");
                e.printStackTrace();
            }

            if (null != photoFile) {
                Uri photoURI = FileProvider.getUriForFile(BasePhotoActivity.this,
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
                    photoAction(bitmap);
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
                photoAction(bitmap);
            }
        }
    }

    protected void detectFaces(final Bitmap imageBitmap, DetectFacesConsumer callback) {
        Log.d(TAG, "detectAndFrame");
        Pair<byte[], Float> imageBytesAndScale;
        try {
            imageBytesAndScale = Util.bitmapToSizeLimitedByteArray(imageBitmap);
        } catch (ImageTooSmall e) {
            Toast.makeText(BasePhotoActivity.this, R.string.image_too_small, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), imageBytesAndScale.first);

        PersonService personService = (PersonService) getApplication();
        Call<List<Face>> request = personService.getFaceService().detectFaces(PersonService.getFaceApiKey(), true, false, null, body);
        Log.d(TAG, "Executing detect faces request");
        detectionProgressDialog.show();
        request.enqueue(new Callback<List<Face>>() {
            @Override
            public void onResponse(@NonNull Call<List<Face>> call, @NonNull Response<List<Face>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Request succeeded");
                    detectionProgressDialog.dismiss();
                    callback.accept(imageBitmap, response.body(), imageBytesAndScale.second);
                } else {
                    Log.d(TAG, "Request failed with code: " + response.code());
                    FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);
                    detectionProgressDialog.setMessage("Detection failed");
                    detectionProgressDialog.dismiss();
                    faceApiFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Face>> call, @NonNull Throwable t) {
                Log.e(TAG, "Detection failed.", t);
                detectionProgressDialog.setMessage("Detection failed");
                detectionProgressDialog.dismiss();
                faceApiFailure();
            }
        });
    }

    protected void faceApiFailure() {
        Toast.makeText(this, R.string.face_api_failure, Toast.LENGTH_SHORT)
                .show();
    }

    protected abstract void photoAction(Bitmap bitmap);

    @FunctionalInterface
    public interface DetectFacesConsumer {
        void accept(Bitmap imageBitmap, List<Face> faces, float scale);
    }
}
