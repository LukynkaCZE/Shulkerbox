package props

import ShulkerboxPaper
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import map.commands.successSound
import map.commands.valueChangeSound
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import selection.SelectionManager.prefix
import toMiniMessage
import util.runLater

class PropListener: Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }

    @EventHandler
    fun tick(event: ServerTickStartEvent) {
        Bukkit.getOnlinePlayers().filter { it.inventory.itemInMainHand == PropManager.moveItem }.forEach {
            val prop = PropManager.propSelections[it] ?: return
            it.sendActionBar("$prefix <yellow>Current Mode: <green><bold>${prop.dragOperation.name}</bold> <dark_gray>| <yellow>Change by <gold>Right/Left-Click (Sneak for precise), <yellow>Rotate mode by <gold>Dropping this Item".toMiniMessage())
        }
    }

    @EventHandler
    fun move(event: PlayerMoveEvent) {
        val player = event.player
        if(player.inventory.itemInMainHand != PropManager.moveItem) return
        val prop = PropManager.propSelections[player] ?: return
        if(prop.dragOperation != PropDragOperation.FREE_MOVE) return

        val location = player.location.add(0.0, 0.5, 0.0)
        val direction = player.location.direction
        location.add(direction.multiply(2).apply { y = 0.0 })
        location.apply { pitch = 0f; yaw = 0f; y = location.y }
        prop.itemDisplay.teleportDuration = 2
        prop.itemDisplay.teleport(location)
    }

    val dontRunPlayers = mutableListOf<Player>()
    @EventHandler
    fun click(event: PlayerInteractEvent) {
        val player = event.player
        if(player.inventory.itemInMainHand != PropManager.moveItem) return
        val prop = PropManager.propSelections[player] ?: return
        event.isCancelled = true
        if(dontRunPlayers.contains(player)) {
            dontRunPlayers.remove(player)
            return
        }

        when(prop.dragOperation) {
            PropDragOperation.ROTATION_X -> rotateProp(prop, event.action, player, VectorDir.X)
            PropDragOperation.ROTATION_Y -> rotateProp(prop, event.action, player, VectorDir.Y)
            PropDragOperation.ROTATION_Z -> rotateProp(prop, event.action, player, VectorDir.Z)
            PropDragOperation.POSITION_X -> translateProp(prop,event.action, player, VectorDir.X)
            PropDragOperation.POSITION_Y -> translateProp(prop,event.action, player, VectorDir.Y)
            PropDragOperation.POSITION_Z -> translateProp(prop,event.action, player, VectorDir.Z)
            PropDragOperation.FREE_MOVE -> return
        }
    }
    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if(event.itemDrop.itemStack != PropManager.moveItem) return
        val prop = PropManager.propSelections[player] ?: return
        event.isCancelled = true

        prop.dragOperation = cyclePropDragOperation(prop.dragOperation)
        player.successSound()
//        player.sendPrefixed("Mode changed to <#ff54ff>${prop.dragOperation.name}")
        dontRunPlayers.add(player)
        val delay: Long = if(prop.dragOperation == PropDragOperation.FREE_MOVE) 10 else 2
        runLater(delay) { dontRunPlayers.remove(player) }
    }

    fun translateProp(prop: SelectedProp, action: Action, player: Player, dir: VectorDir) {
        prop.itemDisplay.interpolationDelay = 0
        prop.itemDisplay.interpolationDuration = 2
        val current = prop.itemDisplay.transformation
        val realValue = if(player.isSneaking) 0.05f else 0.3f
        val value = if(action.isLeftClick) realValue else realValue * -1
        val trans = when(dir) {
            VectorDir.X -> current.translation.apply { x += value } // omg trans rights :3
            VectorDir.Y -> current.translation.apply { y += value } // omg trans rights :3
            VectorDir.Z -> current.translation.apply { z += value } // omg trans rights :3
        }
        player.valueChangeSound()
        prop.itemDisplay.transformation = Transformation(trans, current.leftRotation, current.scale, current.rightRotation)
        return
    }

    fun rotateProp(prop: SelectedProp, action: Action, player: Player, dir: VectorDir) {
        prop.itemDisplay.interpolationDelay = 0
        prop.itemDisplay.interpolationDuration = 2
        val current = prop.itemDisplay.transformation
        val realValue = if(player.isSneaking) 0.05f else 0.3f
        val value = if(action.isLeftClick) realValue else realValue * -1

        val rotationDelta = when(dir) {
            VectorDir.X -> Quaternionf().rotateLocalY(value)
            VectorDir.Y -> Quaternionf().rotateLocalX(value)
            VectorDir.Z -> Quaternionf().rotateLocalZ(value)
        }
        val newRotation = rotationDelta.mul(current.rightRotation)

        player.valueChangeSound()
        prop.itemDisplay.interpolationDelay = 0
        prop.itemDisplay.interpolationDuration = 2
        prop.itemDisplay.transformation = Transformation(current.translation, current.leftRotation, current.scale, newRotation)
        return
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if(PropManager.propSelections[event.player] != null) {
            PropManager.unselect(event.player, false)
        }
    }
}

enum class VectorDir {
    X,
    Y,
    Z
}