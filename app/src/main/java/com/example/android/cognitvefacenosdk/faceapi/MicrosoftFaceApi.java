package com.example.android.cognitvefacenosdk.faceapi;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MicrosoftFaceApi {
    int MIN_IMAGE_BYTES = 1024;

    int MAX_IMAGE_BYTES = 4 * 1024 * 1024;
    
    @POST("face/v1.0/detect")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/octet-stream"
    })
    Call<List<Face>> detectFaces(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Query("returnFaceId") boolean returnFaceId,
            @Query("returnFaceLandmarks") boolean returnFaceLandmarks,
            @Query("returnFaceAttributes") String returnFaceAttributes,
            @Body RequestBody body);

    @POST("face/v1.0/identify")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<List<IdentifyResult>> identifyFaces(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Body IdentifyRequest body);

    @GET("face/v1.0/persongroups/{personGroupId}")
    @Headers({
            "Accept: application/json"
    })
    Call<PersonGroup> getPersonGroup(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Path("personGroupId") String personGroupId);

    @PUT("face/v1.0/persongroups/{personGroupId}")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<Void> createPersonGroup(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Path("personGroupId") String personGroupId,
            @Body PersonGroup body);

    @GET("face/v1.0/persongroups/{personGroupId}/persons")
    @Headers({
            "Accept: application/json",
    })
    Call<List<Person>> listPersonGroupMembers(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Path("personGroupId") String personGroupId);

    @POST("face/v1.0/persongroups/{personGroupId}/train")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<Void> trainPersonGroup(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Path("personGroupId") String personGroupId);

    @GET("face/v1.0/persongroups/{personGroupId}/training")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<TrainingStatus> getPersonGroupTrainingStatus(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Path("personGroupId") String personGroupId);

    @POST("face/v1.0/persongroups/{personGroupId}/persons")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<Person> createPersonGroupPerson(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Path("personGroupId") String personGroupId,
            @Body Person person);

    Call<Void> getPersonGroupPerson();

    @POST("face/v1.0/persongroups/{personGroupId}/persons/{personId}/persistedFaces")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/octet-stream"
    })
    Call<FaceData> addFacePersonGroupPerson(
            @Header("Ocp-Apim-Subscription-Key") String authorization,
            @Path("personGroupId") String personGroupId,
            @Path("personId") String personId,
            @Body RequestBody body);
}
