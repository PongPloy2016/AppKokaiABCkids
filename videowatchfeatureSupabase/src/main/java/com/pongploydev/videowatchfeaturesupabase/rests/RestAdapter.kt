package com.pongploydev.videowatchfeaturesupabase.rests

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import com.google.gson.GsonBuilder
import com.pongploydev.videowatchfeaturesupabase.data.remote.ApiInterfaceV1
import okhttp3.Cache
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

object RestAdapter {

    private var apiInterface: ApiInterface? = null
    private var apiInterfaceV1: ApiInterfaceV1? = null
    private var retrofitInstance: Retrofit? = null

    @Synchronized
    private fun getRetrofit(context: Context?, api_url: String): Retrofit {
        val existing = retrofitInstance
        if (existing != null) {
            return existing
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .cookieJar(object : CookieJar {
                private val cookieManager: CookieManager? by lazy {
                    try {
                        CookieManager.getInstance()
                    } catch (e: Exception) {
                        Log.e("RestAdapter", "CookieManager unavailable", e)
                        null
                    }
                }

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    val urlString = url.toString()
                    val cm = cookieManager ?: return
                    for (cookie in cookies) {
                        try {
                            cm.setCookie(urlString, cookie.toString())
                        } catch (e: Exception) {
                            Log.e("RestAdapter", "Error setting cookie", e)
                        }
                    }
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val cm = cookieManager ?: return emptyList()
                    val cookieHeader = try {
                        cm.getCookie(url.toString())
                    } catch (e: Exception) {
                        null
                    }
                    if (cookieHeader.isNullOrEmpty()) {
                        return emptyList()
                    }
                    val cookies = mutableListOf<Cookie>()
                    val cookiePairs = cookieHeader.split(";")
                    for (pair in cookiePairs) {
                        val cookie = Cookie.parse(url, pair.trim())
                        if (cookie != null) {
                            cookies.add(cookie)
                        }
                    }
                    return cookies
                }
            })
            .addInterceptor(Interceptor { chain ->
                var request = chain.request()
                val url = request.url.toString()
                val cookie = try {
                    CookieManager.getInstance().getCookie(url)
                } catch (e: Exception) {
                    null
                }
                if (!cookie.isNullOrEmpty()) {
                    request = request.newBuilder()
                        .addHeader("Cookie", cookie)
                        .build()
                }
                chain.proceed(request)
            })
            .addInterceptor(Interceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code == 403) {
                    Log.w("CookieChallenge", "HTTP 403 detected. Clearing WebView cookies.")
                    try {
                        CookieManager.getInstance().removeAllCookies(null)
                    } catch (e: Exception) {
                        Log.e("RestAdapter", "Error clearing cookies", e)
                    }
                }
                if (response.body != null) {
                    try {
                        val bodyString = response.peekBody(1024 * 1024).string()
                        Log.d("API_RAW_RESPONSE", bodyString)
                    } catch (e: Exception) {
                        Log.e("API_DEBUG", "Error peeking body: ${e.message}")
                    }
                }
                response
            })
            .hostnameVerifier(HostnameVerifier { hostname, session ->
                HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
            })

        if (context != null) {
            try {
                val cacheDir = File(context.cacheDir, "http_cache")
                val cache = Cache(cacheDir, (10 * 1024 * 1024).toLong())
                okHttpClientBuilder.cache(cache)
            } catch (e: Exception) {
                Log.e("RestAdapter", "Could not create OkHttp Cache", e)
            }
        }

        okHttpClientBuilder.addNetworkInterceptor(Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            val url = chain.request().url.toString()

            if (url.contains("get_total_views")) {
                return@Interceptor originalResponse
            }

            val cacheControl = originalResponse.header("Cache-Control")
            if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")
            ) {
                return@Interceptor originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=300")
                    .removeHeader("Pragma")
                    .build()
            }
            originalResponse
        })

        val okHttpClient = okHttpClientBuilder.build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val baseUrl = if (api_url.endsWith("/")) api_url else "$api_url/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

        retrofitInstance = retrofit
        return retrofit
    }

    @JvmStatic
    fun createAPI(api_url: String): ApiInterface {
        return createAPI(null, api_url)
    }

    @JvmStatic
    fun createAPI(context: Context?, api_url: String): ApiInterface {
        val existing = apiInterface
        if (existing != null) {
            return existing
        }
        val retrofit = getRetrofit(context, api_url)
        val api = retrofit.create(ApiInterface::class.java)
        apiInterface = api
        return api
    }

    @JvmStatic
    fun createAPIV1(context: Context?, api_url: String): ApiInterfaceV1 {
        val existing = apiInterfaceV1
        if (existing != null) {
            return existing
        }
        val retrofit = getRetrofit(context, api_url)
        val api = retrofit.create(ApiInterfaceV1::class.java)
        apiInterfaceV1 = api
        return api
    }
}
