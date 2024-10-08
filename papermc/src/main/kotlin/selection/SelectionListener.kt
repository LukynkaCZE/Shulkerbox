package selection

import ShulkerboxPaper
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import selection.SelectionManager.selectionMap
import send
import sendPrefixed
import util.runLater

@Suppress("UnstableApiUsage")
class SelectionListener: Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }

    @EventHandler
    fun rightClickEvent(event: PlayerInteractEvent) {
        if(!event.hasItem()) return
        if(!event.hasBlock()) return
        if(event.player.inventory.itemInMainHand != SelectionManager.selectionItem) return

        val location = event.clickedBlock!!.location.toBlockLocation()

        var selection: Selection? = selectionMap[event.player]

        if(event.action == Action.RIGHT_CLICK_BLOCK) {
            event.player.sendPrefixed("First point has been selected! <yellow>(1/2)")
            event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, 0.3f, 1f)
            if(selection != null) SelectionManager.remove(event.player)
            selection = SelectionManager.create(event.player, location)
            selection.setFirstPoint(location)
            selection.boundingBoxEntity.setColor(BoundingBoxColor.LIME)
            selection.boundingBoxEntity.setName("${event.player.name}'s Selection")
            event.isCancelled = true
        }

        if(event.action == Action.LEFT_CLICK_BLOCK && selection == null) {
            event.player.send("${SelectionManager.prefix} <red>You need to select first point first!")
            event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.3f, 1f)
            event.isCancelled = true
            return
        }

        if(event.action == Action.LEFT_CLICK_BLOCK) {
            if(selection!!.getSecondPoint() == selection.basePoint) {
                event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, 0.3f, 1.6f)
                event.player.sendPrefixed("Second point has been selected! <yellow>(2/2)")
            } else {
                event.player.playSound(event.player.location, Sound.ITEM_BUNDLE_INSERT, 1.3f, 1f)
                event.player.send("${SelectionManager.prefix} <gray>Second point updated! <yellow>(2/2)")
            }
            selection.setSecondPoint(location)
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onJoin(playerJoinEvent: PlayerJoinEvent) {
        val player = playerJoinEvent.player
        runLater(5) {
            player.gameMode = GameMode.CREATIVE
        }
    }

    @EventHandler
    fun onLeave(playerQuitEvent: PlayerQuitEvent) {
        val player = playerQuitEvent.player
        val selection = selectionMap[player]
        if(selection != null) {
            selection.boundingBoxEntity.dispose()
            selectionMap.remove(player)
        }
    }
}