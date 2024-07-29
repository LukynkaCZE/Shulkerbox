package map

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import props.PropEntity

@Serializable
class ShulkerboxMap(
    val id: String,
    var name: String = id,
    val bounds: MutableMap<String, BoundingBox> = mutableMapOf(),
    val points: MutableMap<String, Point> = mutableMapOf(),
    var props: MutableMap<String, Prop> = mutableMapOf(),
    @Transient
    var loadedProps: MutableList<PropEntity> = mutableListOf(),
    @Transient
    var origin: Location? = null,
    var size: ShulkerboxVector,
    var meta: MutableMap<String, String> = mutableMapOf(),
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
    var brightness: Int,
    var itemStack: PropItemStack
)

fun PropItemStack.toBukkitItemStack(): ItemStack {
    val stack = ItemStack(Material.valueOf(this.material), 1)
    stack.editMeta {
        it.setCustomModelData(this.customModelData)
        it.setEnchantmentGlintOverride(this.enchanted)
    }
    return stack
}

fun ItemStack.toPropItemStack(): PropItemStack {
    return PropItemStack(
        this.type.name,
        if(this.itemMeta.hasCustomModelData()) this.itemMeta.customModelData else 0,
        if(this.itemMeta.hasEnchantmentGlintOverride()) this.itemMeta.enchantmentGlintOverride else false,
    )
}

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

fun ShulkerboxTranform.toTransformation(): Transformation {
    return Transformation(
        this.translation.toBukkitVector().toVector3f(),
        this.leftRotation.toQuaternionf(),
        this.scale.toBukkitVector().toVector3f(),
        this.rightRotation.toQuaternionf()
    )
}

fun Transformation.toShulkerboxTranform(): ShulkerboxTranform {
    return ShulkerboxTranform(
        this.translation.toShulkerboxVector(),
        this.leftRotation.toShulkerboxQuaternionf(),
        this.scale.toShulkerboxVector(),
        this.rightRotation.toShulkerboxQuaternionf()
    )
}
@Serializable
data class ShulkerboxQuaternionf(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 1f,
)

fun Quaternionf.toShulkerboxQuaternionf(): ShulkerboxQuaternionf {
    return ShulkerboxQuaternionf(this.x, this.y, this.z, this.w)
}

fun ShulkerboxQuaternionf.toQuaternionf(): Quaternionf {
    return Quaternionf(this.x, this.y, this.z, this.w)
}

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
) {
    fun toBukkitVector(): Vector {
        return Vector(x, y, z)
    }
}

fun ShulkerboxMap.toJson(): String {
    val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    val out = json.encodeToString<ShulkerboxMap>(this)
    return out
}

fun Vector.toShulkerboxVector(): ShulkerboxVector {
    return ShulkerboxVector(this.x, this.y, this.z)
}

fun Vector3f.toShulkerboxVector(): ShulkerboxVector {
    return ShulkerboxVector(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}

fun Location.toShulkerboxOffset(map: ShulkerboxMap): Vector {
    val offsetVector = Vector(
        this.x - map.origin!!.x,
        this.y - map.origin!!.y,
        this.z - map.origin!!.z
    )
    return offsetVector
}

fun ShulkerboxVector.fromShulkerboxOffset(origin: Location): Location {
    val offsetLocation = Location(
        origin.world,
        this.x + origin.x,
        this.y + origin.y,
        this.z + origin.z
    )
    return offsetLocation
}

@Serializable
data class ShulkerboxBuildServerRegistry(
    val entries: MutableList<ShulkerboxBuildServerRegistryEntry>,
)

@Serializable
data class ShulkerboxBuildServerRegistryEntry(
    val mapId: String,
    val location: ShulkerboxLocation,
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

fun Location.toShulkerboxLocation(): ShulkerboxLocation {
    return ShulkerboxLocation(
        world = this.world.name,
        x = this.x,
        y = this.y,
        z = this.z,
        yaw = this.yaw,
        pitch = this.pitch
    )
}

fun ShulkerboxLocation.toBukkitLocation(): Location {
    return Location(
        Bukkit.getWorld(this.world),
        this.x,
        this.y,
        this.z,
        this.yaw,
        this.pitch
    )
}