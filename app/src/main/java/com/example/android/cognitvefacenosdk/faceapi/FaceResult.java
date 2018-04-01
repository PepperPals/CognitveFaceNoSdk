package com.example.android.cognitvefacenosdk.faceapi;

public class FaceResult {
    private String personId;

    private Float confidence;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "FaceResult{" +
                "personId='" + personId + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
