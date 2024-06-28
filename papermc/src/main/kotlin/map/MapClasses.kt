package map

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.Vector

@Serializable
class ShulkerboxMap(
    val id: String,
    var name: String = id,
    val bounds: MutableMap<String,BoundingBox> = mutableMapOf(),
    val points: MutableMap<String, Point> = mutableMapOf(),
    @Transient
    var origin: Location? = null,
    var size: ShulkerboxVector,
    var meta: MutableMap<String, String> = mutableMapOf()
)

@Serializable
data class BoundingBox(
    var id: String,
    var origin: ShulkerboxVector,
    var size: ShulkerboxVector,
    var meta: MutableMap<String, String> = mutableMapOf()
)

@Serializable
data class Point(
    var id: String,
    var location: ShulkerboxVector,
    var yaw: Float,
    var pitch: Float,
    var type: PointType,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var uid: String
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
    val z: Double
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
    val entries: MutableList<ShulkerboxBuildServerRegistryEntry>
)

@Serializable
data class ShulkerboxBuildServerRegistryEntry(
    val mapId: String,
    val location: ShulkerboxLocation
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