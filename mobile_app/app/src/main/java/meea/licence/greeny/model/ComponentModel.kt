package meea.licence.greeny.model

data class ComponentModel(
    val id: Int? = null,
    val name: String,
    val controllerId: Int,
    val type: ComponentType,
    var active: Boolean
) {
    enum class ComponentType {
        TEMPERATURE_SENSOR,
        MOISTURE_SENSOR,
        ACTUATOR
    }
}
