package com.example.financeappproject;


import com.example.financeappproject.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // USER AUTHENTICATION
    @GET("rest/v1/users")
    Call<List<User>> loginUser(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("email") String email,
            @Query("password_hash") String password
    );
    @POST("rest/v1/users")
    Call<Void> createUser(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body User user
    );

    //  BUDGET MANAGEMENT
    @GET("rest/v1/budgets")
    Call<List<Budget>> getBudgets(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter
    );

    @POST("rest/v1/budgets")
    Call<Void> createBudget(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body Budget budget
    );

    // --- TRANSACTION LOGGING ---
    @POST("rest/v1/transactions")
    Call<Void> logTransaction(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body Transactions transactions
    );
}