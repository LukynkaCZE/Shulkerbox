package props

import ShulkerboxPaper
import cz.lukynka.shulkerbox.common.Prop
import fakes.FakeInteraction
import fakes.FakeItemDisplay
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
    val interaction = FakeInteraction(location)
    val viewerPlayers: MutableSet<Player> = mutableSetOf()
    var moveLocked: Boolean = true

    fun addViewer(player: Player) {
        viewerPlayers.add(player)
        entity.addViewer(player)
        interaction.addViewer(player)
    }

    fun removeViewer(player: Player) {
        entity.removeViewer(player)
        interaction.removeViewer(player)
    }

    init {
        update()
        interaction.addPickHandler { player ->
            PropManager.select(player, this)
        }
    }

    fun update() {
        entity.setItem(prop.itemStack.toBukkitItemStack())
        if (prop.youkaiModelId != null && ShulkerboxPaper.youkaiIntegration) {
            entity.setItem(YoukaiIntegration.getModel(prop.youkaiModelId!!))
        }
        if (prop.brightness != null) {
            entity.setBrightness(Display.Brightness(prop.brightness!!, prop.brightness!!))
        }
        entity.setTransformation(prop.transformation.toTransformation())
        entity.setTransform(ItemDisplay.ItemDisplayTransform.HEAD)
    }

    fun teleport(location: Location) {
        entity.teleport(location)
        interaction.teleport(location.clone().subtract(0.0, 0.5, 0.0))
    }

    fun dispose() {
        entity.despawn()
        interaction.despawn()
    }
}

enum class PropDragOperation(val displayName: String) {
    ROTATION_X("Rotate X"),
    ROTATION_Y("Rotate Y"),
    ROTATION_Z("Rotate Z"),
    POSITION_X("Translation X"),
    POSITION_Y("Translation Y"),
    POSITION_Z("Translation Z"),
    FREE_MOVE("Free Move")
}

fun cyclePropDragOperation(current: PropDragOperation): PropDragOperation {
    val values = PropDragOperation.entries.toTypedArray()
    val nextIndex = (current.ordinal + 1) % values.size
    return values[nextIndex]
}

@Suppress("UNCHECKED_CAST")
fun <T : Enum<T>> nextEntry(currentEntry: T): T {
    val values = currentEntry::class.java.enumConstants as Array<T>
    val currentIndex = values.indexOf(currentEntry)
    val nextIndex = (currentIndex + 1) % values.size
    return values[nextIndex]
}