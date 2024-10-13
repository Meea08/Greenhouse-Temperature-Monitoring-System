package meea.licence.greeny.client_ui.ui.my_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import meea.licence.greeny.MyApp
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.model.UserModel
import meea.licence.greeny.network.RetrofitClient
import meea.licence.greeny.network.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAccountViewModel(private val repository: SharedPreferencesRepository) : ViewModel() {

    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> get() = _userData

    fun setUserData(firstName: String, lastName: String, username: String, email: String) {
        _userData.value = UserModel(firstName, lastName, username, email)
    }

    fun updateUser(userData: UserModel, userId: Int, callback: (UserModel?) -> Unit) {
        val userModel = UserModel(
            firstname = userData.firstname,
            lastname = userData.lastname,
            username = userData.username,
            email = userData.email
        )

        viewModelScope.launch {
            val token = repository.getToken() // Retrieve the token from SharedPreferencesRepository
            token?.let {
                val userService = RetrofitClient.getRetrofitClient(MyApp.getContext(), token).create(UserService::class.java)
                userService.updateUser(userId, userModel).enqueue(object : Callback<UserModel> {
                    override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                        if (response.isSuccessful) {
                            callback(response.body())
                            repository.saveUsername(userData.username)
                            repository.saveEmail(userData.email)
                            repository.saveFirstName(userData.firstname)
                            repository.saveLastName(userData.lastname)
                        } else {
                            callback(null)
                        }
                    }

                    override fun onFailure(call: Call<UserModel>, t: Throwable) {
                        callback(null)
                    }
                })
            }
        }
    }
}
