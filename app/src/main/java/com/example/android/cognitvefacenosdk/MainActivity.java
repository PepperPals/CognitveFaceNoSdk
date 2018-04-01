package com.example.android.cognitvefacenosdk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.example.android.cognitvefacenosdk.faceapi.Face;
import com.example.android.cognitvefacenosdk.faceapi.FaceRectangle;
import com.example.android.cognitvefacenosdk.faceapi.Person;
import com.example.android.cognitvefacenosdk.faceapi.PersonGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends BasePhotoActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PersonService globalState = (PersonService) getApplication();
        if (!globalState.isInitialised()) {
            Log.i(TAG, "Initialising application state");
            globalState.initialise();
            PersonGroup group = new PersonGroup();
            group.setPersonGroupId(globalState.getPersonGroupId());
            group.setName("Cognitive Face Android demo with Retrofit");
            globalState.initPersonGroup(group);
        }

        imageView = findViewById(R.id.imageView1);

        detectionProgressDialog = new ProgressDialog(this);

        Button browseButton = findViewById(R.id.browseButton);
        browseButton.setOnClickListener(v -> startSelectPhoto());

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> startTakePhoto());

        Button addPersonButton = findViewById(R.id.addButton);
        addPersonButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EnrollActivity.class);
            startActivity(intent);
        });
    }


    /*
     * Detect faces by uploading face images
     * Frame faces after detection
     */
    protected void photoAction(Bitmap imageBitmap) {
        detectFaces(imageBitmap, (bitmap, faces, scale) -> {
            ImageView imageView = findViewById(R.id.imageView1);
            imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, faces, null, scale));
            PersonService personService = (PersonService) getApplication();
            personService.identify(personService.getPersonGroupId(), facesToFaceIds(faces), (r) -> {
                Log.i(TAG, "Identification result: "+r);
                Map<String, Person> identifiedPeople = personService.faceIdToPerson(r);
                Log.i(TAG, "Found people: " + identifiedPeople.values());
                imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, faces, identifiedPeople, scale));
            });
        });
    }

    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, List<Face> faces,  Map<String, Person> identifiedPeople, float scale) {
        Log.d(TAG, "drawFaceRectanglesOnBitmap");
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);

        // for drawing rectangles around faces
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);

        // for drawing names next to faces
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(160);

        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            Log.d(TAG, "#faces: " + faces.size());
            for (Face face : faces) {
                FaceRectangle faceRectangle = Util.scaleRectangle(face.getFaceRectangle(), scale);
                canvas.drawRect(
                        faceRectangle.getLeft(),
                        faceRectangle.getTop(),
                        faceRectangle.getRight(),
                        faceRectangle.getBottom(),
                        paint);

                // add name if available
                if (null != identifiedPeople) {
                    final Person person = identifiedPeople.get(face.getFaceId());
                    if (null != person) {
                        Log.d(TAG, "Identified person for faceId: "+face.getFaceId()+" = "+person);
                        final String name = person.getName();
                        if ((null != name) && !"".equals(name.trim())) {
                            canvas.drawText(name, faceRectangle.getLeft(), faceRectangle.getTop(), textPaint);
                        }
                    }
                }
            }
        }
        return bitmap;
    }

    private List<String> facesToFaceIds(List<Face> faces) {
        List<String> faceIds = new ArrayList<>();
        for (Face f : faces) {
            faceIds.add(f.getFaceId());
        }
        return faceIds;
    }
}
