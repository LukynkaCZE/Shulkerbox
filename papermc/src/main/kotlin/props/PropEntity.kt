package props

import ShulkerboxPaper
import fakes.FakeItemDisplay
import map.Prop
import map.toBukkitItemStack
import map.toTransformation
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import youkai.YoukaiIntegration

class PropEntity(var location: Location, var prop: Prop) {
    var dragOperation = PropDragOperation.FREE_MOVE
    val entity: FakeItemDisplay = FakeItemDisplay(location)
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
        entity.setItem(prop.itemStack.toBukkitItemStack())
        if(prop.youkaiModelId != null && ShulkerboxPaper.youkaiSupport) {
            entity.setItem(YoukaiIntegration.getModel(prop.youkaiModelId!!))
        }
        if(prop.brightness != null) {
            entity.setBrightness(Display.Brightness(prop.brightness!!, prop.brightness!!))
        }
        entity.setTransformation(prop.transformation.toTransformation())
        entity.setTransform(ItemDisplay.ItemDisplayTransform.HEAD)
//        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN, true)
//        entity.persistentDataContainer.set(ShulkerboxPaper.shulkerboxPropEntityTag, PersistentDataType.STRING, prop.uid)
    }

    fun dispose() {
        entity.despawn()
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