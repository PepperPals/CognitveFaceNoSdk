package com.example.android.cognitvefacenosdk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.cognitvefacenosdk.faceapi.Face;
import com.example.android.cognitvefacenosdk.faceapi.Person;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EnrollActivity extends BasePhotoActivity {
    private static final String TAG = EnrollActivity.class.getSimpleName();

    @BindView(R.id.browseButton) Button browseButton;

    @BindView(R.id.cameraButton) Button cameraButton;

    @BindView(R.id.enrollButton) Button enrollPersonButton;

    Bitmap faceBitmap = null;

    List<Face> detectedFaces = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        ButterKnife.bind(this);

        imageView = findViewById(R.id.imageView1);

        detectionProgressDialog = new ProgressDialog(this);

        browseButton.setOnClickListener(v -> {
            faceBitmap = null;
            detectedFaces = null;
            startSelectPhoto();
        });

        cameraButton.setOnClickListener(v -> {
            faceBitmap = null;
            detectedFaces = null;
            startTakePhoto();
        });

        enrollPersonButton.setOnClickListener(v -> enrollPerson());
    }

    protected void enrollPerson() {
        if (null != detectedFaces) {
            EditText nameView = findViewById(R.id.person_name);
            final String name = nameView.getText().toString();
            Log.d(TAG, "Enrolling person with name: " + name);

            PersonService personService = (PersonService) getApplication();
            Person person = new Person();
            person.setName(name);
            personService.enrollPerson(personService.getPersonGroupId(), person, faceBitmap);

            // return to main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void photoAction(Bitmap imageBitmap) {
        detectFaces(imageBitmap, (bitmap, faces, scale) -> {
            if ((null != faces) && (0 != faces.size())) {
                if (faces.size() == 1) {
                    faceBitmap = imageBitmap;
                    detectedFaces = faces;
                } else {
                    Log.e(TAG, "Found " + faces.size() + " faces, needed 1");
                    Toast.makeText(EnrollActivity.this, "Found " + faces.size() + " faces, needed 1", Toast.LENGTH_LONG)
                            .show();
                }
            } else {
                Log.e(TAG, "No faces found");
                Toast.makeText(EnrollActivity.this, "No faces detected in selected image", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}
