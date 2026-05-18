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
    @GET("rest/v1/users?select=*")
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

    // BUDGET MANAGEMENT
    @GET("rest/v1/budgets?select=*")
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

    // DEBT MANAGEMENT
    @GET("rest/v1/debts?select=*")
    Call<List<Debt>> getDebts(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter
    );

    @POST("rest/v1/debts")
    Call<Void> createDebt(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body Debt debt
    );

    // FUND MANAGEMENT
    @POST("rest/v1/funds")
    Call<Void> createFund(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body Fund fund
    );

    // TRANSACTION LOGGING
    @GET("rest/v1/transactions?select=*")
    Call<List<Transactions>> getTransactions(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("user_id") String userIdFilter
    );

    @POST("rest/v1/transactions")
    Call<Void> logTransaction(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body Transactions transactions
    );
}
