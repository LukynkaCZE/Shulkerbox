package map

import ShulkerboxPaper
import config.ConfigManager
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import org.bukkit.entity.Player
import props.PropEntity
import selection.BoundingBoxColor
import selection.BoundingBoxEntity
import selection.Selection
import toMiniMessage

class ActiveMapSession(var map: ShulkerboxMap) {

    private val sidebar = ShulkerboxPaper.sidebarLibrary.createSidebar()

    private var mapBoundingBox = BoundingBoxEntity(map.origin!!, map.size.toBukkitVector())
    private val drawableBounds = mutableMapOf<String, BoundingBoxEntity>()
    private val drawablePoints = mutableListOf<MarkerPointEntity>()
    val drawableProps = mutableListOf<PropEntity>()
    private val sidebarEnabled get() = ConfigManager.currentConfig.general.sidebar

    val viewers: MutableList<Player> = mutableListOf()

    init {
        updateDrawables()
        updateSidebar()
    }

    private fun updateSidebar() {
        sidebar.clearLines()
        sidebar.title("<#884dff><bold>Shulkerbox".toMiniMessage())
        sidebar.line(0, "")
        sidebar.line(1, "Editing: <green>${map.name}")
        sidebar.line(2, "Editors:")
        var lastIndex = 2
        this.viewers.take(5).forEachIndexed { index, player ->
            sidebar.line(2 + index + 1, "<gray>- <#c175ff>${player.name}")
            lastIndex = index + 1
        }
        sidebar.line(3 + lastIndex, "")
        sidebar.line(4 + lastIndex, "Objects:")
        sidebar.line(5 + lastIndex, "<gray>- <yellow> ${drawableBounds.size} bounds")
        sidebar.line(6 + lastIndex, "<gray>- <yellow> ${drawablePoints.size} points")
        sidebar.line(7 + lastIndex, "<gray>- <yellow> ${drawableProps.size} props")
        sidebar.line(8 + lastIndex, " ")
    }

    fun addViewer(player: Player) {
        viewers.add(player)
        mapBoundingBox.addViewer(player)
        drawableBounds.forEach { it.value.addViewer(player) }
        drawablePoints.forEach { it.addViewer(player) }
        drawableProps.forEach { it.addViewer(player) }
        if(sidebarEnabled) {
            sidebar.addPlayer(player)
            updateSidebar()
        }
    }

    fun removeViewer(player: Player) {
        viewers.remove(player)
        mapBoundingBox.removeViewer(player)
        drawableBounds.forEach { it.value.removeViewer(player) }
        drawablePoints.forEach { it.removeViewer(player) }
        drawableProps.forEach { it.removeViewer(player) }
        if(sidebarEnabled) {
            sidebar.removePlayer(player)
            updateSidebar()
        }
    }

    fun dispose() {
        mapBoundingBox.dispose()
        drawableBounds.forEach { it.value.dispose() }
        drawableBounds.clear()
        drawablePoints.forEach { it.dispose() }
        drawablePoints.clear()
        drawableProps.forEach { it.dispose() }
        drawableProps.clear()
        sidebar.close()
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

    fun addProp(prop: Prop) {
        map.props[prop.uid] = prop
        updateDrawables()
    }

    fun updateDrawables() {

        mapBoundingBox.dispose()
        mapBoundingBox = BoundingBoxEntity(map.origin!!, map.size.toBukkitVector())
        viewers.forEach { viewer -> mapBoundingBox.addViewer(viewer) }
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
            viewers.forEach { viewer -> boundingBoxEntity.addViewer(viewer) }
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
            val markerEntity = MarkerPointEntity(location, color, it.value)

            drawablePoints.add(markerEntity)
            viewers.forEach { viewer -> markerEntity.addViewer(viewer) }
        }

        drawableProps.forEach { it.dispose() }
        drawableProps.clear()
        map.props.forEach {
            val location = it.value.location.fromShulkerboxOffset(map.origin!!)
            location.yaw = it.value.yaw
            location.pitch = it.value.pitch

            val entity = PropEntity(location, it.value)
            drawableProps.add(entity)
            viewers.forEach { viewer -> entity.addViewer(viewer) }
        }
        updateSidebar()
    }
}

fun Sidebar.line(line: Int, text: String) {
    this.line(line, text.toMiniMessage())
}
