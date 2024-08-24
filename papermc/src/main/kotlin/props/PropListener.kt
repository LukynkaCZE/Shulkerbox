package props

import ShulkerboxPaper
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import map.*
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
            val prop = PropManager.propSelections[it] ?: return@forEach
            val map = MapManager.mapSelections[it] ?: return@forEach
            val propEntity = PropManager.getPropEntityFromProp(prop, map) ?: return@forEach
            it.sendActionBar("$prefix <yellow>Current Mode: <green><bold>${propEntity.dragOperation.name}</bold> <dark_gray>| <yellow>Change by <gold>Right/Left-Click (Sneak for precise), <yellow>Rotate mode by <gold>Dropping this Item".toMiniMessage())
        }
    }
    @EventHandler
    fun move(event: PlayerMoveEvent) {
        val player = event.player
        if(player.inventory.itemInMainHand != PropManager.moveItem) return
        val prop = PropManager.propSelections[player] ?: return
        val map = MapManager.mapSelections[player] ?: return
        val propEntity = PropManager.getPropEntityFromProp(prop, map) ?: return

        if(propEntity.dragOperation != PropDragOperation.FREE_MOVE) return

        val location = player.location.add(0.0, 0.5, 0.0)
        val direction = player.location.direction
        location.add(direction.multiply(2).apply { y = 0.0 })
        location.apply { pitch = 0f; yaw = 0f; y = location.y }
        propEntity.entity.teleport(location)
        prop.location = location.toShulkerboxOffset(map.map).toShulkerboxVector()
    }

    private val cooldown = mutableListOf<Player>()
    @EventHandler
    fun click(event: PlayerInteractEvent) {
        val player = event.player
        if(player.inventory.itemInMainHand != PropManager.moveItem) return
        val prop = PropManager.propSelections[player] ?: return
        val map = MapManager.mapSelections[player] ?: return
        val propEntity = PropManager.getPropEntityFromProp(prop, map) ?: return
        event.isCancelled = true
        if(cooldown.contains(player)) {
            cooldown.remove(player)
            return
        }

        when(propEntity.dragOperation) {
            PropDragOperation.ROTATION_X -> rotateProp(propEntity, event.action, player, VectorDir.X)
            PropDragOperation.ROTATION_Y -> rotateProp(propEntity, event.action, player, VectorDir.Y)
            PropDragOperation.ROTATION_Z -> rotateProp(propEntity, event.action, player, VectorDir.Z)
            PropDragOperation.POSITION_X -> translateProp(propEntity,event.action, player, VectorDir.X)
            PropDragOperation.POSITION_Y -> translateProp(propEntity,event.action, player, VectorDir.Y)
            PropDragOperation.POSITION_Z -> translateProp(propEntity,event.action, player, VectorDir.Z)
            PropDragOperation.FREE_MOVE -> return
        }
    }
    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if(event.itemDrop.itemStack != PropManager.moveItem) return
        val prop = PropManager.propSelections[player] ?: return
        val map = MapManager.mapSelections[player] ?: return
        val propEntity = PropManager.getPropEntityFromProp(prop, map) ?: return
        event.isCancelled = true

        propEntity.dragOperation = cyclePropDragOperation(propEntity.dragOperation)
        player.successSound()
        cooldown.add(player)
        val delay: Long = if(propEntity.dragOperation == PropDragOperation.FREE_MOVE) 10 else 2
        runLater(delay) { cooldown.remove(player) }
    }

    private fun translateProp(prop: PropEntity, action: Action, player: Player, dir: VectorDir) {
        val current = prop.entity.getTransformation()
        val realValue = if(player.isSneaking) 0.05f else 0.3f
        val value = if(action.isLeftClick) realValue else realValue * -1
        val trans = when(dir) {
            VectorDir.X -> current.translation.apply { x += value } // omg trans rights :3
            VectorDir.Y -> current.translation.apply { y += value } // omg trans rights :3
            VectorDir.Z -> current.translation.apply { z += value } // omg trans rights :3
        }
        player.valueChangeSound()
        val transform = Transformation(trans, current.leftRotation, current.scale, current.rightRotation)
        prop.entity.setTransformation(transform)
        prop.prop.transformation = transform.toShulkerboxTranform()
        return
    }

    private fun rotateProp(prop: PropEntity, action: Action, player: Player, dir: VectorDir) {
        val current = prop.entity.getTransformation()
        val realValue = if(player.isSneaking) 0.05f else 0.25f
        val value = if(action.isLeftClick) realValue else realValue * -1

        val rotationDelta = when(dir) {
            VectorDir.X -> Quaternionf().rotateLocalY(value)
            VectorDir.Y -> Quaternionf().rotateLocalX(value)
            VectorDir.Z -> Quaternionf().rotateLocalZ(value)
        }
        val newRotation = rotationDelta.mul(current.rightRotation)

        player.valueChangeSound()
        val transform = Transformation(current.translation, current.leftRotation, current.scale, newRotation)
        prop.entity.setTransformation(transform)
        prop.prop.transformation = transform.toShulkerboxTranform()
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