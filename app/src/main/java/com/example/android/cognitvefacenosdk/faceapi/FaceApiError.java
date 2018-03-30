package com.example.android.cognitvefacenosdk.faceapi;

public class FaceApiError {

    private ErrorDetails error;

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "FaceApiError{" +
                "error=" + error +
                '}';
    }

    public static class ErrorDetails {
        private String code;

        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "ErrorDetails{" +
                    "code='" + code + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
