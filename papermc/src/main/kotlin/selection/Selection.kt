package selection

import ShulkerboxPaper
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.util.Vector
import util.getBoundPositionRelative

class Selection(var basePoint: Location, val player: Player): Listener {

    private var rawBasePoint: Location = basePoint
    private var secondPoint: Location? = null
    var boundingBoxEntity = BoundingBoxEntity(basePoint, Vector(1, 1, 1))

    var selectedSecondPositionProper = false

    init {
        boundingBoxEntity.addViewer(player)
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }

    fun setSecondPoint(location: Location) {
        this.basePoint = getBoundPositionRelative(rawBasePoint, location)
        this.secondPoint = getBoundPositionRelative(location, rawBasePoint)

        boundingBoxEntity.setSize(getBoundingBoxSize())
        boundingBoxEntity.setLocation(getFirstPoint())
    }

    fun setFirstPoint(location: Location) {
        this.basePoint = location
        rawBasePoint = lowestPoint
        boundingBoxEntity.setLocation(getFirstPoint())
        boundingBoxEntity.setSize(getBoundingBoxSize())
    }

    fun getSecondPoint(): Location? {
        return if(secondPoint == null) null else highestPoint
    }

    fun getFirstPoint(): Location {
        return lowestPoint
    }

    val highestPoint: Location
        get() {
            val maxX = maxOf(basePoint.x, secondPoint!!.x)
            val maxY = maxOf(basePoint.y, secondPoint!!.y)
            val maxZ = maxOf(basePoint.z, secondPoint!!.z)

            return Location(basePoint.world, maxX, maxY, maxZ)
        }

    val lowestPoint: Location
        get() {
            val minX = minOf(basePoint.x, secondPoint!!.x)
            val minY = minOf(basePoint.y, secondPoint!!.y)
            val minZ = minOf(basePoint.z, secondPoint!!.z)

            return Location(basePoint.world, minX, minY, minZ)
        }

    fun getBoundingBoxSize(): Vector {
        if(secondPoint == null) return Vector(1, 1, 1)

        val vector = highestPoint.toVector().subtract(lowestPoint.toVector())

//        var x = secondPoint!!.x - basePoint.x
//        var y = secondPoint!!.y - basePoint.y
//        var z = secondPoint!!.z - basePoint.z
//
//        if(secondPoint!!.y > basePoint.y) y += 1.0
//        if(secondPoint!!.y < basePoint.y) y -= 1.0
//
//        if(secondPoint!!.z > basePoint.z) z += 1.0
//        if(secondPoint!!.x > basePoint.x) x += 1.0
//
//        return Vector(x, y, z)
        return vector
    }

    @EventHandler
    fun tick(e: ServerTickStartEvent) {
        val world = basePoint.world
        world.spawnParticle(Particle.ELECTRIC_SPARK, highestPoint, 1, 0.0, 0.0, 0.0, 0.0)
        world.spawnParticle(Particle.ELECTRIC_SPARK, lowestPoint, 1, 0.0, 0.0, 0.0, 0.0)
    }

    fun dispose() {
        boundingBoxEntity.dispose()
        HandlerList.unregisterAll(this)
    }
}