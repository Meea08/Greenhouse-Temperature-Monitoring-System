package meea.licence.greeny.network

import meea.licence.greeny.authentication_ui.model.AuthenticationRequest
import meea.licence.greeny.authentication_ui.model.AuthenticationResponse
import meea.licence.greeny.authentication_ui.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/v1/auth/authenticate")
    fun authenticate(@Body request: AuthenticationRequest): Call<AuthenticationResponse>
    @POST("/api/v1/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthenticationResponse>
}