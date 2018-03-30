package com.example.android.cognitvefacenosdk.faceapi;

public class ImageTooSmall extends Exception {
    public ImageTooSmall(int sizeBytes) {
        super("Image is too small. Size = "+sizeBytes);
    }
}
