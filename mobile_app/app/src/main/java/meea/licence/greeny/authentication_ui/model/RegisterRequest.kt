package meea.licence.greeny.authentication_ui.model

data class RegisterRequest(
    val firstname: String,
    val lastname: String,
    val email: String,
    val username: String,
    val password: String
)
