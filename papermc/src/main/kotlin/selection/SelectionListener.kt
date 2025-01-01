package selection

import ShulkerboxPaper
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import cz.lukynka.shulkerbox.common.BoundingBoxColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import selection.SelectionManager.prefix
import selection.SelectionManager.selectionMap
import sendPrefixed
import toMiniMessage
import util.runLater

@Suppress("UnstableApiUsage")
class SelectionListener: Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        val player = event.player
        if(event.itemDrop.itemStack != SelectionManager.selectionToolItem) return
        val selection: Selection = selectionMap[player] ?: return

        SelectionManager.remove(player)
        player.sendPrefixed("<red>Selection cleared!")
        player.playSound(player.location, Sound.ENTITY_BLAZE_HURT, 0.3f, 2f)
        event.isCancelled = true
    }

    @EventHandler
    fun tick(event: ServerTickEndEvent) {
        Bukkit.getOnlinePlayers().filter { it.inventory.itemInMainHand == SelectionManager.selectionToolItem }.forEach { player ->
            player.sendActionBar("$prefix <yellow>Set position one: <gold>Right-Click <dark_gray>| <yellow>Set position two: <gold>Left-Click <dark_gray>| <yellow>Destroy selection: <gold>Drop (<key:key.drop>)".toMiniMessage())

            val targetBlock = player.getTargetBlockExact(6) ?: return@forEach
            val selection: Selection = selectionMap[player] ?: return@forEach
            if(selection.selectedSecondPositionProper) return@forEach

            val location = targetBlock.location
            selection.setSecondPoint(location)
        }
    }

    @EventHandler
    fun rightClickEvent(event: PlayerInteractEvent) {
        if(!event.hasItem()) return
        if(!event.hasBlock()) return
        if(event.player.inventory.itemInMainHand != SelectionManager.selectionToolItem) return

        val location = event.clickedBlock!!.location.toBlockLocation()

        var selection: Selection? = selectionMap[event.player]

        if(event.action == Action.RIGHT_CLICK_BLOCK) {
            event.player.sendPrefixed("First point has been selected! <yellow>(1/2)")
            event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, 0.3f, 1f)
            if(selection != null) SelectionManager.remove(event.player)
            selection = SelectionManager.create(event.player, location)
            selection.setFirstPoint(location)
            selection.boundingBoxEntity.setColor(BoundingBoxColor.RED)
            selection.boundingBoxEntity.setName("${event.player.name}'s Selection")
            event.isCancelled = true
        }

        if(event.action == Action.LEFT_CLICK_BLOCK && selection == null) {
            event.player.sendPrefixed("<red>You need to select first point first!")
            event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.3f, 1f)
            event.isCancelled = true
            return
        }

        if(event.action == Action.LEFT_CLICK_BLOCK) {
            if(!selection!!.selectedSecondPositionProper) {
                event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, 0.3f, 1.6f)
                event.player.sendPrefixed("Second point has been selected! <yellow>(2/2)")
                selection.boundingBoxEntity.setColor(BoundingBoxColor.LIME)
                selection.selectedSecondPositionProper = true
            } else {
                event.player.playSound(event.player.location, Sound.ITEM_BUNDLE_INSERT, 1.3f, 1f)
                event.player.sendPrefixed("Second point updated! <yellow>(2/2)")
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