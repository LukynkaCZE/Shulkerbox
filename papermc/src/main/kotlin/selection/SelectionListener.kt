package selection

import ShulkerboxPaper
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector
import send
import toMiniMessage
import util.runLater
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

        val location = event.clickedBlock!!.location.toBlockLocation()
        event.player.playSound(event.player.location, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1.3f)

        if(event.action == Action.LEFT_CLICK_BLOCK && !selectionMap.containsKey(event.player)) {
            event.player.send("${SelectionUtil.prefix} <red>You need to select first point first!")
            event.isCancelled = true
            return
        }

        if(event.action == Action.RIGHT_CLICK_BLOCK) {
            event.player.send("${SelectionUtil.prefix} First point selected!")
        }
        val selection = if(!selectionMap.containsKey(event.player)) {
            val sel = Selection(location, event.player)
            sel.setSecondPoint(location)
            selectionMap[event.player] = sel
            sel

        } else selectionMap[event.player]

        if(event.action == Action.LEFT_CLICK_BLOCK) {
            event.player.send("${SelectionUtil.prefix} Second point selected!")
            selection!!.setSecondPoint(location)
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onJoin(playerJoinEvent: PlayerJoinEvent) {
        val player = playerJoinEvent.player
        runLater(5) {
            player.gameMode = GameMode.CREATIVE
            player.inventory.clear()
            player.inventory.setItemInMainHand(SelectionUtil.selectionItem)
        }
    }
}