package meea.licence.greeny.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitESPClient {

    private const val BASE_URL = "http://192.168.4.1" // Replace with your ESP's base URL

    val espService: ESPService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ESPService::class.java)
    }
}
