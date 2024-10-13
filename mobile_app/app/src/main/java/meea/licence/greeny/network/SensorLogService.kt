package meea.licence.greeny.network

import meea.licence.greeny.client_ui.ui.greenhouse_data.SensorLogModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SensorLogService {
    @GET("api/sensorlogs/{sensorId}")
    fun getSensorLogs(@Path("sensorId") sensorId: Int): Call<List<SensorLogModel>>

}

