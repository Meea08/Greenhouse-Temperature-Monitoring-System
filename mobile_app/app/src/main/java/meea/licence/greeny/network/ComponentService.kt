package meea.licence.greeny.network

import meea.licence.greeny.model.ComponentModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ComponentService {
    @POST("/api/component/bulk")
    fun createComponents(@Body components: List<ComponentModel>): Call<List<ComponentModel>>

    @GET("/api/component/controller/{controllerId}")
    fun getComponentsByControllerId(@Path("controllerId") controllerId: Int): Call<List<ComponentModel>>

    @GET("/api/component/active/controller/{controllerId}")
    fun getActiveComponentsByControllerId(@Path("controllerId") controllerId: Int): Call<List<ComponentModel>>

    @PUT("/api/component/controllerId/{controllerId}")
    fun updateComponentsByControllerId(
        @Path("controllerId") controllerId: Int, @Body components: List<ComponentModel>
    ): Call<List<ComponentModel>>
}