package com.example.android.cognitvefacenosdk.faceapi;


public class FaceAttributes {
    int age;
    String gender;
    float smile;
    FacialHair facialHair;
    String glasses;
    Orientation headPose;
    Emotion emotion;

/*          "makeup": {
        "eyeMakeup": true,
                "lipMakeup": false
    },
            "occlusion": {
        "foreheadOccluded": false,
                "eyeOccluded": false,
                "mouthOccluded": false
    },
            "accessories": [
    {"type": "headWear", "confidence": 0.99},
    {"type": "glasses", "confidence": 1.0},
    {"type": "mask"," confidence": 0.87}
      ],
              "blur": {
        "blurLevel": "Medium",
                "value": 0.51
    },
            "exposure": {
        "exposureLevel": "GoodExposure",
                "value": 0.55
    },
            "noise": {
        "noiseLevel": "Low",
                "value": 0.12
    }*/

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public float getSmile() {
        return smile;
    }

    public void setSmile(float smile) {
        this.smile = smile;
    }

    public FacialHair getFacialHair() {
        return facialHair;
    }

    public void setFacialHair(FacialHair facialHair) {
        this.facialHair = facialHair;
    }

    public String getGlasses() {
        return glasses;
    }

    public void setGlasses(String glasses) {
        this.glasses = glasses;
    }

    public Orientation getHeadPose() {
        return headPose;
    }

    public void setHeadPose(Orientation headPose) {
        this.headPose = headPose;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }
}
