package com.ffst.dustbinbrain.kotlin_mvp.di

import com.ffst.dustbinbrain.kotlin_mvp.network.api.ServerAPI
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient
                .Builder()
                .connectTimeout(com.ffst.mvp.config.Config.TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(com.ffst.mvp.config.Config.TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(com.ffst.mvp.config.Config.TIME_OUT, TimeUnit.MILLISECONDS)
                .addInterceptor(get<Interceptor>())
                .build()
    }
    single {
        Retrofit
                .Builder()
                .client(get())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(get()))
                .baseUrl(com.ffst.dustbinbrain.kotlin_mvp.Config.WANG_ANDROID_URL)
                .build()
    }
    single {
        get<Retrofit>().create(ServerAPI::class.java)
    }
}
