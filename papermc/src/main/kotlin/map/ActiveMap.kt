package map

import org.bukkit.entity.Player
import selection.BoundingBoxColor
import selection.BoundingBoxEntity
import selection.Selection

class ActiveMap(var player: Player, var map: ShulkerboxMap) {

    val mapBoundingBox = BoundingBoxEntity(map.origin, map.size)
    val drawableBounds = mutableMapOf<String, BoundingBoxEntity>()

    init {
        mapBoundingBox.setName(map.id)
        mapBoundingBox.setColor(BoundingBoxColor.WHITE)
        updateDrawableBounds()
    }


    fun dispose() {
        mapBoundingBox.remove()
        drawableBounds.forEach { it.value.remove() }
        drawableBounds.clear()
    }

    fun addBound(id: String, selection: Selection) {
        map.bounds[id] = BoundingBox(id, selection.basePoint, selection.getBoundingBoxSize())
        updateDrawableBounds()
    }

    fun removeBound(id: String) {
        map.bounds.remove(id)

    }

    fun updateDrawableBounds() {
        drawableBounds.forEach { it.value.remove() }
        drawableBounds.clear()
        map.bounds.forEach {
            if(drawableBounds.containsKey(it.value.id)) return@forEach
            val boundingBoxEntity = BoundingBoxEntity(it.value.origin, it.value.size)
            boundingBoxEntity.setColor(BoundingBoxColor.YELLOW)
            boundingBoxEntity.setName("${map.id}/${it.value.id}")

            drawableBounds[it.value.id] = boundingBoxEntity
        }
    }
}