import kotlinx.serialization.Serializable

@Serializable
data class ShulkerboxMap(
    val id: String,
    var name: String = id,
    val bounds: MutableMap<String, BoundingBox> = mutableMapOf(),
    val points: MutableMap<String, Point> = mutableMapOf(),
    var props: MutableMap<String, Prop> = mutableMapOf(),
    var size: ShulkerboxVector,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var origin: ShulkerboxLocation? = null
)

@Serializable
data class ShulkerboxLocation(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
)

@Serializable
data class BoundingBox(
    var id: String,
    var origin: ShulkerboxVector,
    var size: ShulkerboxVector,
    var meta: MutableMap<String, String> = mutableMapOf(),
)

@Serializable
data class Prop(
    var uid: String,
    var location: ShulkerboxVector,
    var yaw: Float,
    var pitch: Float,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var transformation: ShulkerboxTranform,
    var brightness: Int?,
    var itemStack: PropItemStack,
    var youkaiModelId: String? = null
)

@Serializable
data class PropItemStack(
    val material: String,
    val customModelData: Int,
    val enchanted: Boolean,
)

@Serializable
data class ShulkerboxTranform(
    val translation: ShulkerboxVector = ShulkerboxVector(0.0, 0.0, 0.0),
    val leftRotation: ShulkerboxQuaternionf = ShulkerboxQuaternionf(),
    val scale: ShulkerboxVector = ShulkerboxVector(1.0, 1.0, 1.0),
    val rightRotation: ShulkerboxQuaternionf = ShulkerboxQuaternionf(),
)

@Serializable
data class ShulkerboxQuaternionf(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 1f,
)

@Serializable
data class Point(
    var id: String,
    var location: ShulkerboxVector,
    var yaw: Float,
    var pitch: Float,
    var type: PointType,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var uid: String,
)

@Serializable
enum class PointType {
    UNIQUE,
    MARKER,
    SPAWN
}

@Serializable
data class ShulkerboxVector(
    val x: Double,
    val y: Double,
    val z: Double,
)
