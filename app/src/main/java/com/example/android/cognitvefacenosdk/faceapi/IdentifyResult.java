package com.example.android.cognitvefacenosdk.faceapi;

import java.util.List;

public class IdentifyResult {
    private String faceId;

    private List<FaceResult> candidates;

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public List<FaceResult> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<FaceResult> candidates) {
        this.candidates = candidates;
    }

    @Override
    public String toString() {
        return "IdentifyResult{" +
                "faceId='" + faceId + '\'' +
                ", candidates=" + candidates +
                '}';
    }
}
