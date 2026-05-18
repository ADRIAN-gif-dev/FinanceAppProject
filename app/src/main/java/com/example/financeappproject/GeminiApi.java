package com.example.financeappproject;

import com.example.financeappproject.models.GeminiRequest;
import com.example.financeappproject.models.GeminiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST
    Call<GeminiResponse> getInsights(
            @Url String url, // Passes the full literal address safely
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}