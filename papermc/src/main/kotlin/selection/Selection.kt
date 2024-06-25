package selection

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import send
import toMiniMessage

class Selection(var basePoint: Location, player: Player) {

    private var secondPoint: Location? = null

    fun setSecondPoint(location: Location) {
        this.secondPoint = location
        boundingBoxEntity.setSize(getBoundingBoxSize())
    }

    fun getSecondPoint(): Location? {
        return secondPoint
    }

    var boundingBoxEntity = BoundingBoxEntity(basePoint, Vector(1, 1, 1))



    fun getBoundingBoxSize(): Vector {
        if(secondPoint == null) return Vector(1, 1, 1)

        var x = secondPoint!!.x - basePoint.x
        var y = secondPoint!!.y - basePoint.y
        var z = secondPoint!!.z - basePoint.z

//        if(secondPoint!!.y == basePoint.y) y = 1.0
        if(secondPoint!!.y > basePoint.y) y += 1.0
        if(secondPoint!!.y < basePoint.y) y -= 1.0

        if(secondPoint!!.z > basePoint.z) z += 1.0
        if(secondPoint!!.x > basePoint.x) x += 1.0
//        if(secondPoint!!.x < basePoint.x) x += 1.0

        Bukkit.broadcast("<gray>$x $y $z | $basePoint | $secondPoint".toMiniMessage())

        return Vector(x, y, z)
    }

    init {
        player.send("${SelectionUtil.prefix} <green>New selection created!")
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1.5f, 1f)
    }
}