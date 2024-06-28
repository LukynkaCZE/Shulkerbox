package map

import org.bukkit.Location
import org.bukkit.util.Vector

class ShulkerboxMap(
    val id: String,
    var name: String = id,
    val bounds: MutableMap<String,BoundingBox> = mutableMapOf(),
    val points: MutableMap<String, Point> = mutableMapOf(),
    var origin: Location,
    var size: Vector,
    var meta: MutableMap<String, String> = mutableMapOf()
)

data class BoundingBox(
    var id: String,
    var origin: Location,
    var size: Vector,
    var meta: MutableMap<String, String> = mutableMapOf()
)

data class Point(
    var id: String,
    var location: Location,
    var yaw: Float,
    var pitch: Float,
    var type: PointType,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var uid: String
)

enum class PointType {
    UNIQUE,
    MARKER,
    SPAWN
}