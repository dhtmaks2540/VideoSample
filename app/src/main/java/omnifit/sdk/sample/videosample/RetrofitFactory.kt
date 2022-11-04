package omnifit.sdk.sample.videosample

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {
    private var instance: Retrofit? = null
    private const val BASE_URL = "https://test.com/"

    fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .baseUrl(BASE_URL)
                .build()
        }

        return instance!!
    }

    fun getOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        client.apply {
            connectTimeout(5, TimeUnit.SECONDS)
            readTimeout(120, TimeUnit.SECONDS)
            addInterceptor(interceptor)
        }

        return client.build()
    }
}