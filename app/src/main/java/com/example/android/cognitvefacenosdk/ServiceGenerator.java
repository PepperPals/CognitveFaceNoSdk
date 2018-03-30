package com.example.android.cognitvefacenosdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
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
}
