package map

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

    val entity: ItemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
    val hitbox: Interaction = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
    val nametag: TextDisplay = location.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay

    init {
        update()
    }

    fun update() {
        entity.setItemStack(ItemStack(color.banner))
        hitbox.interactionHeight = 1f
        entity.setRotation(entity.yaw + 180f, 0f)
        entity.teleport(entity.location.clone().apply { y += 0.25f })
        entity.transformation = Transformation(Vector3f(), AxisAngle4f(), Vector3f(0.5f, 0.5f, 0.5f), AxisAngle4f())
        nametag.isShadowed = false
        nametag.alignment = TextDisplay.TextAlignment.CENTER
//        nametag.text("${point.id} <gray>(${point.uid})".toMiniMessage().style { it.color(color.textColor) })
        nametag.text("${point.id})".toMiniMessage().style { it.color(color.textColor) })
        nametag.teleport(entity.location.clone().apply { y += 0.85f })
        nametag.billboard = Display.Billboard.CENTER

        nametag.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
        hitbox.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
    }

    fun dispose() {
        entity.remove()
        hitbox.remove()
        nametag.remove()
    }
}