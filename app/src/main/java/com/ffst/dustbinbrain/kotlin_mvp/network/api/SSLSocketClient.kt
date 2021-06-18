package com.ffst.dustbinbrain.kotlin_mvp.network.api

import java.security.SecureRandom
import javax.net.ssl.*
import javax.security.cert.CertificateException
import javax.security.cert.X509Certificate

/**
 * Created by LiuJW
 *on 2021/6/15
 */
class SSLSocketClient {
    companion object{
        //获取这个SSLSocketFactory
        fun getSSLSocketFactory(): SSLSocketFactory? {
            return try {
                val sslContext: SSLContext = SSLContext.getInstance("SSL")
                sslContext.init(null, getTrustManager(), SecureRandom())
                sslContext.getSocketFactory()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        //获取TrustManager
        private fun getTrustManager(): Array<TrustManager>? {
            return arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })
        }

        fun getX509TrustManager(): X509TrustManager? {
            return object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                    TODO("Not yet implemented")
                }

                override fun checkServerTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                    TODO("Not yet implemented")
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate?> {
                    return arrayOfNulls(0)
                }
            }
        }

        fun getHostnameVerifier(): HostnameVerifier? {
            return HostnameVerifier { s, ssl -> true }
        }
    }
}