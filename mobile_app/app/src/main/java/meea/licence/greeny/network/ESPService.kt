package meea.licence.greeny.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface ESPService {
    @FormUrlEncoded
    @POST("/config")
    fun sendCredentials(
        @Field("ssid") ssid: String,
        @Field("password") password: String
    ): Call<Void>

    @FormUrlEncoded
    @POST("/id")
    fun sendId(
        @Field("id") id: String
    ): Call<Void>
}
