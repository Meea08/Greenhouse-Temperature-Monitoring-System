package meea.licence.greeny

import android.content.Context

class SharedPreferencesRepository(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun saveToken(token: String) {
        with(sharedPreferences.edit()) {
            putString("auth_token", token)
            apply()
        }
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    fun saveUserId(userId: Int) {
        with(sharedPreferences.edit()) {
            putInt("user_id", userId)
            apply()
        }
    }

    fun saveUsername(username: String) {
        with(sharedPreferences.edit()) {
            putString("username", username)
            apply()
        }
    }

    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    fun saveFirstName(firstName: String) {
        with(sharedPreferences.edit()) {
            putString("firstname", firstName)
            apply()
        }
    }

    fun getFirstName(): String? {
        return sharedPreferences.getString("firstname", null)
    }

    fun saveLastName(lastName: String) {
        with(sharedPreferences.edit()) {
            putString("lastname", lastName)
            apply()
        }
    }

    fun getLastName(): String? {
        return sharedPreferences.getString("lastname", null)
    }

    fun saveEmail(email: String) {
        with(sharedPreferences.edit()) {
            putString("email", email)
            apply()
        }
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("email", null)
    }

    fun saveRole(role: String) {
        with(sharedPreferences.edit()) {
            putString("role", role)
            apply()
        }
    }

    fun getRole(): String? {
        return sharedPreferences.getString("role", null)
    }

    fun clearUserData(): Boolean{
        return try {
            with(sharedPreferences.edit()) {
                remove("auth_token")
                remove("user_id")
                remove("username")
                remove("firstname")
                remove("lastname")
                remove("email")
                remove("role")
                apply()
                true
            }
        }
        catch (e: Exception) {
            false
        }
    }
}
