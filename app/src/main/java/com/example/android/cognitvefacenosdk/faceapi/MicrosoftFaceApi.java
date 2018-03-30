package com.example.android.cognitvefacenosdk.faceapi;

import java.io.InputStream;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MicrosoftFaceApi {
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
}
