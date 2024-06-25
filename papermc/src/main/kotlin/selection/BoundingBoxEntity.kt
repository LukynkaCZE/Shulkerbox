package selection

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f



class BoundingBoxEntity(initialLocation: Location, initialSize: Vector) {

    private var location = initialLocation
    private var color = BoundingBoxColor.WHITE
    private var name = "Selection"
    private var size = initialSize

    fun setLocation(location: Location) {
        this.location = location
        update()
    }

    fun setColor(color: BoundingBoxColor) {
        this.color = color
        update()
    }

    fun setName(name: String) {
        this.name = name
        update()
    }

    fun setSize(size: Vector) {
        this.size = size
        update()
    }

    fun getLocation(): Location {
        return location
    }

    fun getColor(): BoundingBoxColor {
        return color
    }

    fun getName(): String {
        return name
    }

    fun getSize(): Vector {
        return size
    }


    private val entity: BlockDisplay = location.world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay
    private val flipped: BlockDisplay = location.world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay

    init {

    }

    fun update() {

        val translation = Vector3f()
        val scale = size.toVector3f()

        if(scale.x == 0f) scale.x = 1f
        if(scale.y == 0f) scale.y = 1f
        if(scale.z == 0f) scale.z = 1f

        if(scale.y < 0f) {
            translation.y += 1f
        }

        if(scale.x < 0f) {
            translation.x += 1f
            scale.x -= 1f
        }

        if(scale.z < 0f) {
            translation.z += 1f
            scale.z -= 1f
        }

        scale.add(0.01f, 0.01f, 0.01f)

        entity.brightness = Display.Brightness(15, 15)
        entity.transformation = Transformation(translation, AxisAngle4f(), scale, AxisAngle4f())
        entity.isGlowing = true
        entity.block = Material.RED_STAINED_GLASS.createBlockData()
//        entity.block = color.block.createBlockData()



        flipped.brightness = Display.Brightness(15, 15)
        flipped.transformation = Transformation(translation, AxisAngle4f(), scale.mul(-1f), AxisAngle4f())
        flipped.isGlowing = true
        flipped.block = Material.LIGHT_BLUE_STAINED_GLASS.createBlockData()
    }
}

enum class BoundingBoxColor(val block: Material, val color: Color) {
    RED(Material.RED_STAINED_GLASS, Color.RED),
    ORANGE(Material.ORANGE_STAINED_GLASS, Color.ORANGE),
    YELLOW(Material.YELLOW_STAINED_GLASS, Color.YELLOW),
    LIME(Material.LIME_STAINED_GLASS, Color.LIME),
    AQUA(Material.YELLOW_STAINED_GLASS, Color.YELLOW),
    PINK(Material.PINK_STAINED_GLASS, Color.FUCHSIA),
    PURPLE(Material.PURPLE_STAINED_GLASS, Color.PURPLE),
    WHITE(Material.WHITE_STAINED_GLASS, Color.WHITE),
}