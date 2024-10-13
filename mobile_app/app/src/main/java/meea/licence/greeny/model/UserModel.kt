package meea.licence.greeny.model

data class UserModel(
    val id: Int?=null,
    val firstname: String,
    val lastname: String,
    val username: String,
    val email: String,
    val password: String?=null,
    val role: Role?=null

) {
    constructor( firstname: String, lastname: String, username: String, email: String) : this(null, firstname, lastname, username, email, null, null)

    enum class Role {
        ADMIN,
        CLIENT
    }
}
