package com.example.musicappexercise6.api

import com.example.musicappexercise6.untils.Constants.BASE_URL
import com.example.musicappexercise6.untils.Constants.FILTER_URL
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object{
        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("dd/MM/yyyy")
                .create()
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        private val retrofitFilter by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("dd/MM/yyyy")
                .create()
            Retrofit.Builder()
                .baseUrl(FILTER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        val api: MusicAPI by lazy {
            retrofit.create(MusicAPI::class.java)
        }
        val apiFilter: FilterAPI by lazy {
            retrofitFilter.create(FilterAPI::class.java)
        }
    }
}