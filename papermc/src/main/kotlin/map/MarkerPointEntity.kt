package map

import fakes.FakeItemDisplay
import org.bukkit.Location
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import selection.BoundingBoxColor
import toMiniMessage

class MarkerPointEntity(var location: Location, var color: BoundingBoxColor, var point: Point) {

    val entity: FakeItemDisplay = FakeItemDisplay(location)
    val nametag: TextDisplay = location.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay

    val viewerPlayers: MutableSet<Player> = mutableSetOf()

    fun addViewer(player: Player) {
        viewerPlayers.add(player)
        entity.addViewer(player)
    }

    fun removeViewer(player: Player) {
        entity.removeViewer(player)
    }

    init {
        update()
    }

    fun update() {
        entity.setItem(ItemStack(color.banner))
        entity.setRotation(entity.entity.bukkitYaw - 180f, 0f)
        entity.teleport(entity.location.clone().apply { y += 0.25f })
        entity.setTransformation(Transformation(Vector3f(), AxisAngle4f(), Vector3f(0.5f, 0.5f, 0.5f), AxisAngle4f()))
        nametag.isShadowed = false
        nametag.alignment = TextDisplay.TextAlignment.CENTER
        nametag.text("${point.id}\n<gray>(${point.uid})".toMiniMessage().style { it.color(color.textColor) })
        nametag.teleport(entity.location.clone().apply { y += 0.85f })
        nametag.billboard = Display.Billboard.CENTER

        nametag.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
//        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
    }

    fun dispose() {
        entity.despawn()
        nametag.remove()
    }
}