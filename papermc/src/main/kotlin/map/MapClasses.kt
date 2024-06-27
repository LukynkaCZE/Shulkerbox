package map

import org.bukkit.Location
import org.bukkit.util.Vector

class ShulkerboxMap(
    val id: String,
    val name: String = id,
    val bounds: MutableMap<String,BoundingBox> = mutableMapOf(),
    val points: MutableList<Point> = mutableListOf(),
    val origin: Location,
    val size: Vector
) {
}

data class BoundingBox(
    var id: String,
    var origin: Location,
    var size: Vector,
)


data class Point(
    var id: String,
    var location: Location,
    var yaw: Float,
    var pitch: Float,
    var type: PointType
)

enum class PointType {
    UNIQUE,
    MARKER,
    SPAWN
}

data class ShulkerboxMetadata(val key: String, val type: ShulkerboxMetadataType, val value: Any)

enum class ShulkerboxMetadataType {
    STRING,
    FLOAT,
    INT,
    LONG,
    DOUBLE,
    VECTOR3,
    BOOLEAN,
    UUID,
    BYTE_ARRAY
}