package com.example.financeappproject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Replace this with your actual Project URL from Supabase Settings > API
    private static final String BASE_URL = "https://gblkyeuixhrtrtcxnwjq.supabase.co/";
    private static Retrofit retrofit = null;

    public static SupabaseApi getSupabaseApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseApi.class);
    }
}