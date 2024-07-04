package map

import org.bukkit.entity.Player
import selection.BoundingBoxColor
import selection.BoundingBoxEntity
import selection.Selection

class ActiveMapSession(var player: Player, var map: ShulkerboxMap) {

    var mapBoundingBox = BoundingBoxEntity(map.origin!!, map.size.toBukkitVector())
    val drawableBounds = mutableMapOf<String, BoundingBoxEntity>()
    val drawablePoints = mutableListOf<MarkerPointEntity>()

    init {
        updateDrawables()
    }

    fun dispose() {
        mapBoundingBox.dispose()
        drawableBounds.forEach { it.value.dispose() }
        drawableBounds.clear()
        drawablePoints.forEach { it.dispose() }
        drawablePoints.clear()
    }

    fun addBound(id: String, selection: Selection) {
        map.bounds[id] = BoundingBox(id, selection.basePoint.toShulkerboxOffset(map).toShulkerboxVector(), selection.getBoundingBoxSize().toShulkerboxVector())
        updateDrawables()
    }

    fun removeBound(id: String) {
        map.bounds.remove(id)
        updateDrawables()
    }

    fun addPoint(point: Point) {
        map.points[point.uid] = point
        updateDrawables()
    }

    fun updateDrawables() {

        mapBoundingBox.dispose()
        mapBoundingBox = BoundingBoxEntity(map.origin!!, map.size.toBukkitVector())
        val name = buildString {
            append("${map.name} (${map.id})")
            map.meta.forEach { append("\n<green>${it.key} <gray>= <aqua>${it.value}") }
        }

        mapBoundingBox.setName(name)
        mapBoundingBox.setColor(BoundingBoxColor.WHITE)

        drawableBounds.forEach { it.value.dispose() }
        drawableBounds.clear()
        map.bounds.forEach {
            if(drawableBounds.containsKey(it.value.id)) return@forEach
            val boundingBoxEntity = BoundingBoxEntity(it.value.origin.fromShulkerboxOffset(map.origin!!), it.value.size.toBukkitVector())
            val color = BoundingBoxColor.YELLOW
            boundingBoxEntity.setColor(color)
            val boundName = buildString {
                append("${map.id}/${it.value.id}")
                it.value.meta.forEach { append("\n<green>${it.key} <gray>= <aqua>${it.value}") }
            }
            boundingBoxEntity.setName(boundName)

            drawableBounds[it.value.id] = boundingBoxEntity
        }

        drawablePoints.forEach { it.dispose() }
        drawablePoints.clear()
        map.points.forEach {
            val color = when(it.value.type) {
                PointType.UNIQUE -> BoundingBoxColor.AQUA
                PointType.MARKER -> BoundingBoxColor.WHITE
                PointType.SPAWN -> BoundingBoxColor.PINK
            }
            val location = it.value.location.fromShulkerboxOffset(map.origin!!)
            location.yaw = it.value.yaw
            location.pitch = it.value.pitch
            val entity = MarkerPointEntity(location, color, it.value)

            drawablePoints.add(entity)
        }
    }
}