package com.example.android.cognitvefacenosdk.faceapi;

public class PersonGroup {
    private String personGroupId;

    private String name;

    private String userData;

    public String getPersonGroupId() {
        return personGroupId;
    }

    public void setPersonGroupId(String personGroupId) {
        this.personGroupId = personGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return "PersonGroup{" +
                "personGroupId='" + personGroupId + '\'' +
                ", name='" + name + '\'' +
                ", userData='" + userData + '\'' +
                '}';
    }
}
