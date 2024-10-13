package meea.licence.greeny.authentication_ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.android.material.snackbar.Snackbar
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.model.UserModel
import meea.licence.greeny.network.RetrofitClient
import meea.licence.greeny.network.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseAuthActivity : AppCompatActivity() {

    protected lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesRepository = SharedPreferencesRepository(this)
    }

    protected fun decodeToken(token: String): DecodedJWT? {
        return try {
            JWT.decode(token)
        } catch (e: JWTDecodeException) {
            logError("AUTH", "Failed to decode token: ${e.message}")
            null
        }
    }

    protected fun fetchUserDetailsWithToken(username: String, callback: (UserModel) -> Unit) {
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("auth_token", null)
        token?.let {
            val userService = RetrofitClient.getRetrofitClient(this, token).create(UserService::class.java)
            userService.getUserByUsername(username).enqueue(object :
                Callback<UserModel> {
                override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                    if (response.isSuccessful) {
                        response.body()?.let { callback(it) } ?: run {
                            Log.e("AUTH", "Failed to fetch user details: empty response body")
                        }
                    } else {
                        Log.e("AUTH", "Failed to fetch user details: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<UserModel>, t: Throwable) {
                    Log.e("AUTH", "Failed to fetch user details: ${t.message}")
                }
            })
        } ?: run {
            logError("USER LOG IN", "Token is missing")
        }
    }

    protected fun saveUserInfo(user: UserModel) {
        sharedPreferencesRepository.saveUserId(user.id ?: -1)
        sharedPreferencesRepository.saveUsername(user.username)
        sharedPreferencesRepository.saveFirstName(user.firstname)
        sharedPreferencesRepository.saveLastName(user.lastname)
        sharedPreferencesRepository.saveEmail(user.email)
        sharedPreferencesRepository.saveRole(user.role.toString())
    }

    protected fun saveToken(token: String) {
        sharedPreferencesRepository.saveToken(token)
    }

    protected fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    protected fun logError(tag: String, message: String) {
        Log.e(tag, message)
    }

    protected fun extractAndSaveUserId(token: String) {
        val decodedToken = decodeToken(token)
        decodedToken?.let {
            val userId = it.getClaim("userId").asLong()
            if (userId != null) {
                sharedPreferencesRepository.saveUserId(userId.toInt())
            }
        } ?: logError("AUTH", "Failed to extract userId from token")
    }
}
