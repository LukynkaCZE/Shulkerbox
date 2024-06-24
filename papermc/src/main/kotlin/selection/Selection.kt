package selection

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import send

class Selection(var basePoint: Location, player: Player) {

    var secondPoint: Location? = null

    var boundingBoxEntity = BoundingBoxEntity(basePoint, Vector(1, 1, 1))

    init {
        player.send("${SelectionUtil.prefix} new selection created")
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f)
    }

}