package com.helpyourself.com.di

import android.content.Context
import android.util.Log
import com.helpyourself.com.BuildConfig
import com.helpyourself.com.data.api.CustomModelService
import com.helpyourself.com.data.api.TavilyService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /* ---------- constants ---------- */
    private const val SERVER_PORT      = "5002"
    private const val EMULATOR_URL     = "http://10.0.2.2:$SERVER_PORT/"
    private const val LOCAL_DEVICE_URL = "http://192.168.29.103:$SERVER_PORT/"
    private const val TAVILY_BASE_URL  = "https://api.tavily.com/"

    /* ---------- base URL for your own backend ---------- */
    @Provides
    @Singleton
    @Named("baseUrl")
    fun baseUrl(@ApplicationContext ctx: Context): String {
        val isEmulator = android.os.Build.PRODUCT.contains("sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK")
        return if (isEmulator) EMULATOR_URL else LOCAL_DEVICE_URL
    }

    /* ---------- logging ---------- */
    @Provides
    @Singleton
    fun logging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    /* ---------- generic OkHttp ---------- */
    @Provides
    @Singleton
    fun okHttp(log: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(log)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    /* ======================  YOUR OWN BACKEND  ====================== */

    @Provides
    @Singleton
    @Named("customRetrofit")
    fun customRetrofit(
        okHttpClient: OkHttpClient,
        @Named("baseUrl") baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    @Provides
    @Singleton
    fun customModelService(
        @Named("customRetrofit") retrofit: Retrofit
    ): CustomModelService = retrofit.create(CustomModelService::class.java)

    /* ========================  TAVILY  ======================== */

    /* OkHttp that appends the Tavily API key */
    @Provides
    @Singleton
    @Named("tavilyOkHttp")
    fun tavilyOkHttp(base: OkHttpClient): OkHttpClient =
        base.newBuilder()
            .addInterceptor(Interceptor { chain ->
                val authorised = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.TAVILY_API_KEY}")
                    .build()
                chain.proceed(authorised)
            })
            .build()

    /* Retrofit for Tavily */
    @Provides
    @Singleton
    @Named("tavilyRetrofit")
    fun tavilyRetrofit(
        @Named("tavilyOkHttp") okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(TAVILY_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /* Tavily service interface */
    @Provides
    @Singleton
    fun tavilyService(
        @Named("tavilyRetrofit") retrofit: Retrofit
    ): TavilyService = retrofit.create(TavilyService::class.java)
}