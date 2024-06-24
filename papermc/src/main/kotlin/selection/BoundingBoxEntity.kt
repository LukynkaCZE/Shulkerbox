package selection

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f

class BoundingBoxEntity(location: Location, size: Vector) {

    init {
        val entity = location.world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay

        entity.interpolationDuration = 5
        entity.transformation = Transformation(Vector3f(), AxisAngle4f(), size.toVector3f(), AxisAngle4f())
        entity.isGlowing = true
        entity.block = Material.RED_STAINED_GLASS.createBlockData()

        val flipped = location.world.spawnEntity(location.add(size), EntityType.BLOCK_DISPLAY) as BlockDisplay
        flipped.interpolationDuration = 5
        flipped.isGlowing = true
        flipped.transformation = Transformation(Vector3f(), AxisAngle4f(), size.toVector3f().sub(Vector3f(-0.01f)).mul(-1f), AxisAngle4f())
        flipped.block = Material.RED_STAINED_GLASS.createBlockData()

    }

}