package meea.licence.greeny.network

import meea.licence.greeny.model.GHControllerModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GHControllerService {
    @GET("api/controller/userId/{userId}")
    fun getControllerByUserId(@Path("userId") userId: Int): Call<List<GHControllerModel>>
    @PUT("api/controller/id/{controllerId}")
    fun updateController(@Path("controllerId") controllerId: Int,
                         @Body controllerData: GHControllerModel): Call<GHControllerModel>
    @DELETE("api/controller/id/{controllerId}")
    fun deleteController(@Path("controllerId") controllerId: Int): Call<Void>
    @POST("api/controller")
    fun createController(@Body newGHController: GHControllerModel): Call<GHControllerModel>
}