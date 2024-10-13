package meea.licence.greeny.client_ui.ui.change_password

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import meea.licence.greeny.MyApp
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.network.RetrofitClient
import meea.licence.greeny.network.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordViewModel(private val repository: SharedPreferencesRepository) : ViewModel() {

    fun changePassword(userId: Int, oldPassword: String, newPassword: String, callback: (Boolean) -> Unit) {
        val changePasswordRequest = ChangePasswordRequest(oldPassword, newPassword)

        viewModelScope.launch {
            val token = repository.getToken()
            Log.d("ChangePasswordViewModel", "Token: $token")
            if (token.isNullOrEmpty()) {
                Log.e("ChangePasswordViewModel", "Token is null or empty")
                callback(false)
                return@launch
            }

            val userService = RetrofitClient.getRetrofitClient(MyApp.getContext(), token).create(UserService::class.java)
            userService.changePassword(userId, changePasswordRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("ChangePasswordViewModel", "Response code: ${response.code()}")
                    callback(response.isSuccessful)
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("ChangePasswordViewModel", "Network request failed", t)
                    callback(false)
                }
            })
        }
    }
}

