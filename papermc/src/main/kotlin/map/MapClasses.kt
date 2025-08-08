package map

import cz.lukynka.shulkerbox.common.*
import kotlinx.serialization.Serializable
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
        if (this.itemMeta.hasCustomModelDataComponent()) this.itemMeta.customModelDataComponent.floats.first().toInt() else 0,
        if (this.itemMeta.hasEnchantmentGlintOverride()) this.itemMeta.enchantmentGlintOverride else false,
    )
}

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

fun Quaternionf.toShulkerboxQuaternionf(): ShulkerboxQuaternionf {
    return ShulkerboxQuaternionf(this.x, this.y, this.z, this.w)
}

fun ShulkerboxQuaternionf.toQuaternionf(): Quaternionf {
    return Quaternionf(this.x, this.y, this.z, this.w)
}

fun ShulkerboxVector.toBukkitVector(): Vector {
    return Vector(x, y, z)
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