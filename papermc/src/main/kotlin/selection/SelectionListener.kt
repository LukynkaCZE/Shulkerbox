package selection

import ShulkerboxPaper
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import toMiniMessage
import java.util.UUID

class SelectionListener: Listener {

    val selectionMap = mutableMapOf<Player, Selection>()

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }

    @EventHandler
    fun rightClickEvent(event: PlayerInteractEvent) {
        if(!event.hasItem()) return
        if(!event.hasBlock()) return
        if(event.player.inventory.itemInMainHand != SelectionUtil.selectionItem) return

        val location = event.clickedBlock!!.location
        val selection = if(!selectionMap.containsKey(event.player)) {
            val sel = Selection(location, event.player)
            selectionMap[event.player] = sel
            sel

        } else selectionMap[event.player]
    }
}