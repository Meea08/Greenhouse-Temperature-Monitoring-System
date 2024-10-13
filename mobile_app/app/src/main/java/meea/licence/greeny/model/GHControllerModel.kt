package meea.licence.greeny.model

data class GHControllerModel(
    val id: Int?=null,
    val userId: Int,
    var name: String,
    var minThreshold: Double,
    var maxThreshold: Double
)
