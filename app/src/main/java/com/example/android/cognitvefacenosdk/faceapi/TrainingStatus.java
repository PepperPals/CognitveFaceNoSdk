package com.example.android.cognitvefacenosdk.faceapi;

public class TrainingStatus {
    private String status;

    private String createdDateTime;

    private String lastActionDateTime;

    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getLastActionDateTime() {
        return lastActionDateTime;
    }

    public void setLastActionDateTime(String lastActionDateTime) {
        this.lastActionDateTime = lastActionDateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TrainingStatus{" +
                "status='" + status + '\'' +
                ", createdDateTime='" + createdDateTime + '\'' +
                ", lastActionDateTime='" + lastActionDateTime + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
