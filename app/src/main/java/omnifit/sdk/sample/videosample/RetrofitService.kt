package omnifit.sdk.sample.videosample

import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitService {
    @Multipart
    @POST("주소")
    fun postVideo()
}