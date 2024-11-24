package map

import ShulkerboxAnnotation
import ShulkerboxMap
import fakes.FakeTextDisplay
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player

class AnnotationEntity(var location: Location, var annotation: ShulkerboxAnnotation, val map: ShulkerboxMap) {

    val textDisplay: FakeTextDisplay = FakeTextDisplay(location)

    val viewerPlayers: MutableSet<Player> = mutableSetOf()

    fun addViewer(player: Player) {
        viewerPlayers.add(player)
        textDisplay.addViewer(player)
        update()
    }

    fun removeViewer(player: Player) {
        viewerPlayers.remove(player)
        textDisplay.removeViewer(player)
        update()
    }

    init {
        update()
    }

    fun update() {
        textDisplay.setText("<#cc0202><bold>Annotation</bold> <gray>(${annotation.uid})\n\n<#fc7474>${annotation.text.replace(";", "\n")}")
        textDisplay.setBillboard(Display.Billboard.CENTER)
        textDisplay.setBackground(0)
        textDisplay.setShadow(true)
        textDisplay.teleport(annotation.location.fromShulkerboxOffset(map.origin!!.toBukkitLocation()))
    }

    fun dispose() {
        textDisplay.despawn()
    }
}