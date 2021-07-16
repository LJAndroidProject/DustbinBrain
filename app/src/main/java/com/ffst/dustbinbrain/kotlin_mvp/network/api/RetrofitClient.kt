package com.ffst.dustbinbrain.kotlin_mvp.network.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


/**
 * Created by LiuJW
 *on 2021/6/15
 */
class RetrofitClient {
    companion object{
        private val REWRITE_CACHE_CONTROL_INTERCEPTOR_JSON: Interceptor? = Interceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header("Content-Type", "application/json;charset=UTF-8")
                .build()
            val t1 = System.nanoTime()
            val response: Response = chain.proceed(request)
            val t2 = System.nanoTime()
            val content: String = response.body!!.string()

            response.newBuilder().body(ResponseBody.create(response.body!!.contentType(),content)).build()
        }

        private val REWRITE_CACHE_CONTROL_INTERCEPTOR_FORM: Interceptor? = Interceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()
            val t1 = System.nanoTime()
            val response: Response = chain.proceed(request)
            val t2 = System.nanoTime()
            val content: String = response.body!!.string()
            response.newBuilder().body(ResponseBody.create(response.body!!.contentType(),content)).build()
        }

        private val REWRITE_CACHE_CONTROL_INTERCEPTOR_IMAGE: Interceptor? = Interceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()
            val t1 = System.nanoTime()
            val response: Response = chain.proceed(request)
            val t2 = System.nanoTime()
            val content: String = response.body!!.string()
            response.newBuilder().body(ResponseBody.create(response.body!!.contentType(),content)).build()
        }


        private val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR_FORM!!)
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier()!!)
            .sslSocketFactory(
                SSLSocketClient.getSSLSocketFactory()!!,
                SSLSocketClient.getX509TrustManager()!!
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        private val clientJson: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR_JSON!!)
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier()!!)
            .sslSocketFactory(
                SSLSocketClient.getSSLSocketFactory()!!,
                SSLSocketClient.getX509TrustManager()!!
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        fun buildFormClient(url: String?): Retrofit? {
            return Retrofit.Builder().baseUrl(url).client(client).build()
        }

        fun buildJsonClient(url: String?):Retrofit?{
            return Retrofit.Builder().baseUrl(url).client(clientJson).build()
        }

        fun createBodyWithJson(args: String?): RequestBody? {
            return args!!
                .toRequestBody("application/json;charset=UTF-8".toMediaTypeOrNull())
        }
    }
}