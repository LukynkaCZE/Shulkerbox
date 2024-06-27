package map

import org.bukkit.Location
import org.bukkit.util.Vector

class ShulkerboxMap(
    val id: String,
    val name: String = id,
    val bounds: MutableMap<String,BoundingBox> = mutableMapOf(),
    val origin: Location,
    val size: Vector
) {
}

data class BoundingBox(
    val id: String,
    val origin: Location,
    val size: Vector,
)