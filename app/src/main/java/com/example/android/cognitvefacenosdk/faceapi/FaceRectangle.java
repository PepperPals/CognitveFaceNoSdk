package com.example.android.cognitvefacenosdk.faceapi;

public class FaceRectangle {
    int width;
    int height;
    int left;
    int top;

    public int getRight() {
        return left + width;
    }

    public int getBottom() {
        return top + height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
}
