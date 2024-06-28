package selection

import ShulkerboxPaper
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f
import toMiniMessage


class BoundingBoxEntity(val initialLocation: Location, val initialSize: Vector) {

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

    private val entity: ItemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
    private val flipped: ItemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
    private val nametag: TextDisplay = location.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay

    fun update() {

        nametag.text(name.toMiniMessage().style { it.color(color.textColor) })
        nametag.isShadowed = false
        nametag.isSeeThrough = true
        nametag.billboard = Display.Billboard.CENTER
        nametag.alignment = TextDisplay.TextAlignment.CENTER

        val centerOffset = size.clone().divide(Vector(2f, 2f, 2f))
        val textLocation = location.clone().add(centerOffset)

        if(textLocation.y < location.y) {
            textLocation.y += 1
        }

        nametag.teleport(textLocation)

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

        val item = ItemStack(Material.STICK)
        item.editMeta { it.setCustomModelData(color.customModelData) }

        entity.interpolationDelay = 0
        entity.interpolationDuration = 5
        flipped.interpolationDelay = 0
        flipped.interpolationDuration = 5

        entity.brightness = Display.Brightness(15, 15)
        entity.transformation = Transformation(translation.copy(), AxisAngle4f(), scale.copy(), AxisAngle4f())
        entity.isGlowing = true
        entity.setItemStack(item)
        entity.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
        entity.isGlowing = false
        entity.isGlowing = true
        entity.glowColorOverride = color.color

        flipped.brightness = Display.Brightness(15, 15)
        flipped.transformation = Transformation(scale.copy().add(translation), AxisAngle4f(), scale.copy().mul(-1f), AxisAngle4f())
        flipped.setItemStack(item)
        flipped.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD

        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
        flipped.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
        nametag.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
    }

    fun dispose() {
        entity.remove()
        flipped.remove()
        nametag.remove()
    }
}

enum class BoundingBoxColor(val customModelData: Int, val color: Color, val textColor: TextColor, val banner: Material) {
    RED(1, Color.BLUE, NamedTextColor.RED, Material.RED_BANNER),
    ORANGE(2, Color.TEAL, NamedTextColor.GOLD, Material.ORANGE_BANNER),
    YELLOW(3, Color.AQUA, NamedTextColor.YELLOW, Material.YELLOW_BANNER),
    LIME(4, Color.LIME, NamedTextColor.GREEN, Material.LIME_BANNER),
    AQUA(5, Color.YELLOW, NamedTextColor.AQUA, Material.LIGHT_BLUE_BANNER),
    PINK(7, Color.FUCHSIA, NamedTextColor.LIGHT_PURPLE, Material.PINK_BANNER),
    PURPLE(6, Color.PURPLE, NamedTextColor.DARK_PURPLE, Material.PURPLE_BANNER),
    WHITE(8, Color.WHITE, NamedTextColor.WHITE, Material.WHITE_BANNER),
}

fun Vector3f.copy(): Vector3f {
    return Vector(this.x.toDouble(), this.y.toDouble(), this.z.toDouble()).toVector3f()
}

fun Vector3f.toVector(): Vector {
    return Vector(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}