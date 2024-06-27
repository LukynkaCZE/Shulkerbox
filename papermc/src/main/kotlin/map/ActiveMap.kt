package map

import org.bukkit.entity.Player
import selection.BoundingBoxColor
import selection.BoundingBoxEntity
import selection.Selection

class ActiveMap(var player: Player, var map: ShulkerboxMap) {

    val mapBoundingBox = BoundingBoxEntity(map.origin, map.size)
    val drawableBounds = mutableMapOf<String, BoundingBoxEntity>()
    val drawablePoints = mutableListOf<MarkerPointEntity>()

    init {
        mapBoundingBox.setName(map.id)
        mapBoundingBox.setColor(BoundingBoxColor.WHITE)
        updateDrawables()
    }

    fun dispose() {
        mapBoundingBox.dispose()
        drawableBounds.forEach { it.value.dispose() }
        drawableBounds.clear()
    }

    fun addBound(id: String, selection: Selection) {
        map.bounds[id] = BoundingBox(id, selection.basePoint, selection.getBoundingBoxSize())
        updateDrawables()
    }

    fun removeBound(id: String) {
        map.bounds.remove(id)
        updateDrawables()
    }

    fun addPoint(point: Point) {
        map.points.add(point)
        updateDrawables()
    }

    fun updateDrawables() {
        drawableBounds.forEach { it.value.dispose() }
        drawableBounds.clear()
        map.bounds.forEach {
            if(drawableBounds.containsKey(it.value.id)) return@forEach
            val boundingBoxEntity = BoundingBoxEntity(it.value.origin, it.value.size)
            val color = BoundingBoxColor.YELLOW
            boundingBoxEntity.setColor(color)
            boundingBoxEntity.setName("${map.id}/${it.value.id}")

            drawableBounds[it.value.id] = boundingBoxEntity
        }

        drawablePoints.forEach { it.dispose() }
        drawablePoints.clear()
        map.points.forEach {
            if(it.type != PointType.SPAWN) {
                drawablePoints.add(MarkerPointEntity(it.location, BoundingBoxColor.WHITE, it))
            }
        }

    }
}