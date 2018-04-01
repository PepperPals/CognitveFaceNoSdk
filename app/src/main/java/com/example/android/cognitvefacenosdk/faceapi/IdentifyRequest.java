package com.example.android.cognitvefacenosdk.faceapi;

import java.util.List;

public class IdentifyRequest {
    private String personGroupId;

    private List<String> faceIds;

    private Integer maxNumOfCandidatesReturned;

    private Float confidenceThreshold;

    public String getPersonGroupId() {
        return personGroupId;
    }

    public void setPersonGroupId(String personGroupId) {
        this.personGroupId = personGroupId;
    }

    public List<String> getFaceIds() {
        return faceIds;
    }

    public void setFaceIds(List<String> faceIds) {
        this.faceIds = faceIds;
    }

    public Integer getMaxNumOfCandidatesReturned() {
        return maxNumOfCandidatesReturned;
    }

    public void setMaxNumOfCandidatesReturned(Integer maxNumOfCandidatesReturned) {
        this.maxNumOfCandidatesReturned = maxNumOfCandidatesReturned;
    }

    public Float getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(Float confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    @Override
    public String toString() {
        return "IdentifyRequest{" +
                "personGroupId='" + personGroupId + '\'' +
                ", faceIds=" + faceIds +
                ", maxNumOfCandidatesReturned=" + maxNumOfCandidatesReturned +
                ", confidenceThreshold=" + confidenceThreshold +
                '}';
    }
}
