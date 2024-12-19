package map

import BoundingBoxColor
import Point
import fakes.FakeItemDisplay
import fakes.FakeTextDisplay
import org.bukkit.Location
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import selection.getBoundingBoxColorData
import toMiniMessage

class MarkerPointEntity(var location: Location, var color: BoundingBoxColor, var point: Point) {

    val entity: FakeItemDisplay = FakeItemDisplay(location)
    val nametag: FakeTextDisplay = FakeTextDisplay(location)

    val viewerPlayers: MutableSet<Player> = mutableSetOf()

    fun addViewer(player: Player) {
        viewerPlayers.add(player)
        entity.addViewer(player)
        nametag.addViewer(player)
    }

    fun removeViewer(player: Player) {
        entity.removeViewer(player)
        nametag.removeViewer(player)
    }

    init {
        update()
    }

    fun update() {
        entity.setItem(ItemStack(getBoundingBoxColorData(color).banner))
        entity.setRotation(entity.entity.bukkitYaw - 180f, 0f)
        entity.teleport(entity.location.clone().apply { y += 0.25f })
        entity.setTransformation(Transformation(Vector3f(), AxisAngle4f(), Vector3f(0.5f, 0.5f, 0.5f), AxisAngle4f()))
        nametag.setText("${point.id}\n<gray>(${point.uid})".toMiniMessage().style { it.color(getBoundingBoxColorData(color).textColor) })
        nametag.teleport(entity.location.clone().apply { y += 0.85f })
        nametag.setBillboard(Display.Billboard.CENTER)
    }

    fun dispose() {
        entity.despawn()
        nametag.despawn()
    }
}