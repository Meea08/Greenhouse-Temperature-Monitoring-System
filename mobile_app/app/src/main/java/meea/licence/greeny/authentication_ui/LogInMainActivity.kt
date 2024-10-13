package meea.licence.greeny.authentication_ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.auth0.jwt.interfaces.DecodedJWT
import meea.licence.greeny.R
import meea.licence.greeny.authentication_ui.model.AuthenticationRequest
import meea.licence.greeny.authentication_ui.model.AuthenticationResponse
import meea.licence.greeny.client_ui.MainClientActivity
import meea.licence.greeny.network.AuthService
import meea.licence.greeny.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogInMainActivity : BaseAuthActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var invalidInputTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        signUpTextView = findViewById(R.id.textViewSignUp)
        invalidInputTextView = findViewById(R.id.textViewInvalidInput)

        // Check if user is already logged in
        checkLoggedInUser()

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            authenticateUser(username, password)
        }

        signUpTextView.setOnClickListener {
            goToRegisterActivity()
        }
    }

    private fun checkLoggedInUser() {
        val token = sharedPreferencesRepository.getToken()
        if (token != null) {
            decodeToken(token)?.let { decodedJWT ->
                // Assuming token is valid and not expired
                handleLoginSuccess(decodedJWT, "") // Username not needed here
                goToClientMainActivity() // Automatically log in the user
            } ?: run {
                // Invalid token, proceed with login
            }
        }
    }

    private fun authenticateUser(username: String, password: String) {
        invalidInputTextView.text = ""
        if (username.isEmpty() || password.isEmpty()) {
            logError("USER LOG IN", "Empty fields")
            invalidInputTextView.setText(R.string.empty_field)
            return
        }

        val authRequest = AuthenticationRequest(username, password)
        val authService = RetrofitClient.getRetrofitClient(this, "").create(AuthService::class.java)
        authService.authenticate(authRequest)
            .enqueue(object : Callback<AuthenticationResponse> {
                override fun onResponse(call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { authResponse ->
                            val token = authResponse.token
                            decodeToken(token)?.let { decodedJWT ->
                                handleLoginSuccess(decodedJWT, username)
                                saveToken(token)
                                extractAndSaveUserId(token) // Extract and save user ID
                            } ?: run {
                                invalidInputTextView.setText(R.string.invalid_token)
                            }
                        } ?: run {
                            logError("USER LOG IN", "Invalid credentials")
                            invalidInputTextView.setText(R.string.invalid_credentials)
                        }
                    } else {
                        logError("USER LOG IN", "Invalid credentials")
                        invalidInputTextView.setText(R.string.invalid_credentials)
                    }
                }

                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    logError("USER LOG IN", "Login failed: ${t.message}")
                    showSnackbar("Login failed. Please try again.")
                }
            })
    }

    private fun handleLoginSuccess(decodedJWT: DecodedJWT, username: String) {
        val rolesClaim = decodedJWT.getClaim("roles").asList(Map::class.java)
        rolesClaim?.let {
            val roles = it.mapNotNull { role -> role["authority"] as? String }
            fetchUserDetailsWithToken(username) { user ->
                saveUserInfo(user)
                when {
                    roles.contains("ADMIN") -> goToAdminMainActivity()
                    roles.contains("CLIENT") -> goToClientMainActivity()
                    else -> invalidInputTextView.setText(R.string.unknown_role)
                }
            }
        } ?: run {
            logError("USER LOG IN", "Unknown roles")
            invalidInputTextView.setText(R.string.unknown_role)
        }
    }

    private fun goToClientMainActivity() {
        startActivity(Intent(this, MainClientActivity::class.java))
        finish()
    }

    private fun goToAdminMainActivity() {
        // Implement admin activity navigation
    }

    private fun goToRegisterActivity() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}

