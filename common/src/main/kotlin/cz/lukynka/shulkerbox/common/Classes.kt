package cz.lukynka.shulkerbox.common

import kotlinx.serialization.Serializable

const val CURRENT_SHULKERBOX_VERSION = 3

@Serializable
data class ShulkerboxMap(
    var version: Int,
    val id: String,
    var name: String = id,
    val bounds: MutableMap<String, BoundingBox> = mutableMapOf(),
    val points: MutableMap<String, Point> = mutableMapOf(),
    var props: MutableMap<String, Prop> = mutableMapOf(),
    var size: ShulkerboxVector,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var schematicToOriginOffset: ShulkerboxVector,
    var origin: ShulkerboxLocation? = null,
    var annotations: MutableMap<String, ShulkerboxAnnotation>? = null,
//    var particleSpewers: MutableMap<String, ParticleSpewer> = mutableMapOf()
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
    var buildServerColor: BoundingBoxColor? = BoundingBoxColor.YELLOW
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

//@Serializable
//data class ParticleSpewer(
//    val uid: String,
//    var location: ShulkerboxVector,
//    var particle: String,
//    var amount: Int,
//    val speed: Float,
//    val offset: ShulkerboxVector,
//    val intervalTicks: Int,
//)

@Serializable
data class ShulkerboxAnnotation(
    var uid: String,
    var location: ShulkerboxVector,
    val text: String
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
    SPAWN,
    PARTICLE_SPEWER,
}

@Serializable
data class ShulkerboxVector(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    constructor(x: Int, y: Int, z: Int): this(x.toDouble(), y.toDouble(), z.toDouble())

    fun offsetTo(vector: ShulkerboxVector): ShulkerboxVector {
        return ShulkerboxVector(
            vector.x - this.x,
            vector.y - this.y,
            vector.z - this.z
        )
    }
}

@Serializable
enum class BoundingBoxColor(val customModelData: Int) {
    RED(1),
    ORANGE(2),
    YELLOW(3),
    LIME(4),
    AQUA(5),
    PINK(7),
    PURPLE(6),
    WHITE(8),
}