package com.example.android.cognitvefacenosdk.faceapi;


public class Face {
    private String faceId;
    private FaceRectangle faceRectangle;
    private FaceLandmarks faceLandmarks;
    private FaceAttributes faceAttributes;

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public FaceRectangle getFaceRectangle() {
        return faceRectangle;
    }

    public void setFaceRectangle(FaceRectangle faceRectangle) {
        this.faceRectangle = faceRectangle;
    }

    public FaceLandmarks getFaceLandmarks() {
        return faceLandmarks;
    }

    public void setFaceLandmarks(FaceLandmarks faceLandmarks) {
        this.faceLandmarks = faceLandmarks;
    }

    public FaceAttributes getFaceAttributes() {
        return faceAttributes;
    }

    public void setFaceAttributes(FaceAttributes faceAttributes) {
        this.faceAttributes = faceAttributes;
    }
}
