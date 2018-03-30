package com.example.android.cognitvefacenosdk;

import android.util.Log;

import com.example.android.cognitvefacenosdk.faceapi.FaceApiError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    private static final String TAG = ServiceGenerator.class.getSimpleName();

    private static final String MS_BASE_URL = "https://westeurope.api.cognitive.microsoft.com/";

    static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    static GsonConverterFactory gsonFactory = GsonConverterFactory.create();

    static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .build();

    public static Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(MS_BASE_URL)
            .addConverterFactory(gsonFactory)
            .build();

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public static FaceApiError parseFaceApiError(Response<?> response) {
        Converter<ResponseBody, FaceApiError> converter =
                ServiceGenerator.retrofit
                        .responseBodyConverter(FaceApiError.class, new Annotation[0]);

        FaceApiError error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            Log.e(TAG, "Enable to parse error response", e);
            return new FaceApiError();
        }

        return error;
    }
}
