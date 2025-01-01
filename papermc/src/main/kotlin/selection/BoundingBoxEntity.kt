package selection

import cz.lukynka.shulkerbox.common.BoundingBoxColor
import fakes.FakeItemDisplay
import fakes.FakeTextDisplay
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f
import toMiniMessage


class BoundingBoxEntity(initialLocation: Location, initialSize: Vector) {

    private var location = initialLocation
    private var color = BoundingBoxColor.WHITE
    private var name = "Selection"
    private var size = initialSize

    private val entity: FakeItemDisplay = FakeItemDisplay(location)
    private val flipped: FakeItemDisplay = FakeItemDisplay(location)
    private val nametag: FakeTextDisplay = FakeTextDisplay(location)

    val viewerPlayers: MutableSet<Player> = mutableSetOf()

    fun addViewer(player: Player) {
        viewerPlayers.add(player)
        entity.addViewer(player)
        flipped.addViewer(player)
        nametag.addViewer(player)
    }

    fun removeViewer(player: Player) {
        viewerPlayers.remove(player)
        entity.removeViewer(player)
        flipped.removeViewer(player)
        nametag.removeViewer(player)
    }

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


    fun update() {

        nametag.setText("<${getBoundingBoxColorData(color).textColor.asHexString()}> $name")
        nametag.setBillboard(Display.Billboard.CENTER)
        nametag.setSeeThrough(true)

        entity.teleport(location)
        flipped.teleport(location)

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

        entity.setBrightness(Display.Brightness(15, 15))
        entity.setTransformation(Transformation(translation.copy(), AxisAngle4f(), scale.copy(), AxisAngle4f()))
        entity.setGlowing(true)
        entity.setItem(item)
        entity.setTransform(ItemDisplay.ItemDisplayTransform.HEAD)
        entity.setGlowColor(getBoundingBoxColorData(color).color)

        flipped.setBrightness(Display.Brightness(15, 15))
        flipped.setTransformation(Transformation(scale.copy().add(translation), AxisAngle4f(), scale.copy().mul(-1f), AxisAngle4f()))
        flipped.setItem(item)
        flipped.setTransform(ItemDisplay.ItemDisplayTransform.HEAD)
    }

    fun dispose() {
        entity.despawn()
        flipped.despawn()
        nametag.despawn()
    }
}

data class BoundingBoxColorData(val customModelData: Int, val color: Color, val textColor: TextColor, val banner: Material)

fun getBoundingBoxColorData(boundingBoxColor: BoundingBoxColor): BoundingBoxColorData {
    return when(boundingBoxColor) {
        BoundingBoxColor.RED -> BoundingBoxColorData(1, Color.RED, NamedTextColor.RED, Material.RED_BANNER)
        BoundingBoxColor.ORANGE -> BoundingBoxColorData(2, Color.ORANGE, NamedTextColor.GOLD, Material.ORANGE_BANNER)
        BoundingBoxColor.YELLOW -> BoundingBoxColorData(3, Color.YELLOW, NamedTextColor.YELLOW, Material.YELLOW_BANNER)
        BoundingBoxColor.LIME -> BoundingBoxColorData(4, Color.LIME, NamedTextColor.GREEN, Material.LIME_BANNER)
        BoundingBoxColor.AQUA -> BoundingBoxColorData(5, Color.AQUA, NamedTextColor.AQUA, Material.LIGHT_BLUE_BANNER)
        BoundingBoxColor.PINK -> BoundingBoxColorData(7, Color.FUCHSIA, NamedTextColor.LIGHT_PURPLE, Material.PINK_BANNER)
        BoundingBoxColor.PURPLE -> BoundingBoxColorData(6, Color.PURPLE, NamedTextColor.DARK_PURPLE, Material.PURPLE_BANNER)
        BoundingBoxColor.WHITE -> BoundingBoxColorData(8, Color.WHITE, NamedTextColor.WHITE, Material.WHITE_BANNER)
    }
}

fun Vector3f.copy(): Vector3f {
    return Vector(this.x.toDouble(), this.y.toDouble(), this.z.toDouble()).toVector3f()
}

fun Vector3f.toVector(): Vector {
    return Vector(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}