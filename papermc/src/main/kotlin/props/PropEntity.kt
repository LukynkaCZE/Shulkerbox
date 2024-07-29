package props

import map.Prop
import map.toBukkitItemStack
import map.toTransformation
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.persistence.PersistentDataType

class PropEntity(var location: Location, var prop: Prop) {
    var dragOperation = PropDragOperation.FREE_MOVE
    val entity: ItemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay

    init {
        update()
    }

    fun update() {
        entity.setItemStack(prop.itemStack.toBukkitItemStack())
        if(prop.brightness != null) {
            entity.brightness = Display.Brightness(prop.brightness!!, prop.brightness!!)
        }
        entity.teleportDuration = 2
        entity.interpolationDelay = 2
        entity.interpolationDuration = 2
        entity.transformation = prop.transformation.toTransformation()
        entity.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxPropEntityTag, PersistentDataType.STRING, prop.uid)
    }

    fun dispose() {
        entity.remove()
    }
}

enum class PropDragOperation {
    ROTATION_X,
    ROTATION_Y,
    ROTATION_Z,
    POSITION_X,
    POSITION_Y,
    POSITION_Z,
    FREE_MOVE
}

fun cyclePropDragOperation(current: PropDragOperation): PropDragOperation {
    val values = PropDragOperation.entries.toTypedArray()
    val nextIndex = (current.ordinal + 1) % values.size
    return values[nextIndex]
}