package props

import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player

class SelectedProp(var itemDisplay: ItemDisplay, var player: Player) {
    var dragOperation = PropDragOperation.FREE_MOVE
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