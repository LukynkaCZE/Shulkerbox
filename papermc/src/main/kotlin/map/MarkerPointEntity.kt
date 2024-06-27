package map

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import selection.BoundingBoxColor
import toMiniMessage

class MarkerPointEntity(var location: Location, var color: BoundingBoxColor, var point: Point) {

    val entity: ItemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
    val hitbox: Interaction = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction

    init {
        update()
    }

    fun update() {
        entity.setItemStack(ItemStack(color.banner))
        hitbox.interactionHeight = 2f
        entity.setRotation(entity.yaw + 180f, 0f)
        entity.teleport(entity.location.clone().apply { y += 0.5f })
        entity.customName(point.id.toMiniMessage().style { it.color(color.textColor) })
        entity.isCustomNameVisible = true
        
        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
        hitbox.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
    }

    fun dispose() {
        entity.remove()
        hitbox.remove()
    }
}