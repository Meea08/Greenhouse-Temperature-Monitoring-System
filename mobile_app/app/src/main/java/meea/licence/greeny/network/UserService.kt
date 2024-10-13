package meea.licence.greeny.network

import meea.licence.greeny.client_ui.ui.change_password.ChangePasswordRequest
import meea.licence.greeny.model.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("api/users/username/{username}")
    fun getUserByUsername(@Path("username") username: String): Call<UserModel>
    @PUT("api/users/id/{userId}")
    fun updateUser(@Path("userId") id: Int,
                   @Body user: UserModel
    ): Call<UserModel>
    @PUT("api/users/change-password/{userId}")
    fun changePassword(
        @Path("userId") id: Int,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Call<Void>
}