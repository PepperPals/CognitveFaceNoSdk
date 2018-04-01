package com.example.android.cognitvefacenosdk.faceapi;

public class FaceData {
    private String persistedFaceId;

    public String getPersistedFaceId() {
        return persistedFaceId;
    }

    public void setPersistedFaceId(String persistedFaceId) {
        this.persistedFaceId = persistedFaceId;
    }

    @Override
    public String toString() {
        return "FaceData{" +
                "persistedFaceId='" + persistedFaceId + '\'' +
                '}';
    }
}
