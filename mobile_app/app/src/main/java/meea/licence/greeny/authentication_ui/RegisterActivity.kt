package meea.licence.greeny.authentication_ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.auth0.jwt.interfaces.DecodedJWT
import meea.licence.greeny.R
import meea.licence.greeny.authentication_ui.model.AuthenticationResponse
import meea.licence.greeny.authentication_ui.model.RegisterRequest
import meea.licence.greeny.client_ui.MainClientActivity
import meea.licence.greeny.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : BaseAuthActivity() {

    private lateinit var submitButton: Button
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var logInTextView: TextView
    private lateinit var errorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        submitButton = findViewById(R.id.buttonRegister)
        logInTextView = findViewById(R.id.textViewLogIn)
        errorTextView = findViewById(R.id.textViewError)
        firstNameEditText = findViewById(R.id.editTextFirstName)
        lastNameEditText = findViewById(R.id.editTextLastName)
        usernameEditText = findViewById(R.id.editTextUsername)
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)

        submitButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password == confirmPassword) {
                registerUser(firstName, lastName, username, email, password)
            } else {
                errorTextView.text = getString(R.string.passwords_do_not_match)
            }
        }

        logInTextView.setOnClickListener {
            goToLogInMainActivity()
        }
    }

    private fun registerUser(
        firstName: String, lastName: String, username: String, email: String, password: String
    ) {
        errorTextView.text = ""
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorTextView.text = getString(R.string.fields_cannot_be_empty)
            return
        }

        val registerRequest = RegisterRequest(firstName, lastName, email, username, password)
        RetrofitClient.authService.register(registerRequest)
            .enqueue(object : Callback<AuthenticationResponse> {
                override fun onResponse(
                    call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { authResponse ->
                            decodeToken(authResponse.token)?.let { decodedJWT ->
                                saveToken(authResponse.token)
                                handleRegisterSuccess(authResponse.token, decodedJWT, username)
                            } ?: run {
                                errorTextView.setText(R.string.invalid_token)
                            }
                        } ?: run {
                            logError("USER REGISTER", "Invalid credentials")
                            errorTextView.setText(R.string.invalid_credentials)
                        }
                    } else {
                        logError("USER REGISTER", "Invalid credentials")
                        errorTextView.setText(R.string.invalid_credentials)
                    }
                }

                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    logError("USER REGISTER", "Register failed: ${t.message}")
                    showSnackbar("Register failed. Please try again.")
                }
            })
    }

    private fun handleRegisterSuccess(token: String, decodedJWT: DecodedJWT, username: String) {
        val rolesClaim = decodedJWT.getClaim("roles").asList(Map::class.java)
        rolesClaim?.let {
            val roles = it.mapNotNull { role -> role["authority"] as? String }
            fetchUserDetailsWithToken(token) { user ->
                saveUserInfo(user)
                when {
                    roles.contains("ADMIN") -> goToAdminMainActivity()
                    roles.contains("CLIENT") -> goToClientMainActivity()
                    else -> errorTextView.setText(R.string.unknown_role)
                }
            }
        } ?: run {
            logError("USER REGISTER", "Unknown roles")
            errorTextView.setText(R.string.unknown_role)
        }
    }

    private fun goToClientMainActivity() {
        startActivity(Intent(this, MainClientActivity::class.java))
        finish()
    }

    private fun goToAdminMainActivity() {
        // Implement admin activity navigation
    }

    private fun goToLogInMainActivity() {
        startActivity(Intent(this, LogInMainActivity::class.java))
    }

}

