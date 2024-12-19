package tools

import ShulkerboxPaper
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import map.commands.playChangeSound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import props.nextEntry
import selection.SelectionManager.prefix
import toMiniMessage
import util.runLater

object SuperBarrierTool: Listener {

    val superBarrierToolItem = ItemStack(Material.BARRIER)

    private val storedHeight = mutableMapOf<Player, Height>()

    init {

        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)

        superBarrierToolItem.editMeta {
            it.displayName("<red><u>Super Barrier Tool".toMiniMessage())
            it.lore(
                mutableListOf(
                    " ".toMiniMessage(),
                    "<gray>Builds a variable height barrier wall".toMiniMessage(),
                    "<gray>Change the height by <aqua>Dropping the Item (<key:key.drop>)".toMiniMessage(),
                    " ".toMiniMessage(),
                    "<dark_gray>Shulkerbox Map Manager Item".toMiniMessage()
                )
            )
            it.setEnchantmentGlintOverride(true)
            it.setMaxStackSize(1)
        }
    }

    @EventHandler
    fun tick(event: ServerTickEndEvent) {
        Bukkit.getOnlinePlayers().filter { it.inventory.itemInMainHand == superBarrierToolItem }.forEach { player ->
            var currentHeight = storedHeight[player]
            if(currentHeight == null) {
                storedHeight[player] = Height.HEIGHT_10
                currentHeight = Height.HEIGHT_10
            }
            player.sendActionBar("$prefix <yellow>Height: <aqua><bold>${currentHeight.height}</bold> <dark_gray>| <yellow>Change by <gold>Dropping this Item (<key:key.drop>)".toMiniMessage())
        }
    }

    // This is fix the stupid fucking multive verse thing
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val gamemode = event.player.gameMode
        var isFlying = event.player.isFlying
        runLater(3) {
            event.player.gameMode = gamemode
            if(event.player.location.block.isEmpty) isFlying = true
            event.player.isFlying = isFlying
        }
    }

    @EventHandler()
    fun onPlace(event: BlockPlaceEvent) {
        if(event.itemInHand != superBarrierToolItem) return

        val player = event.player
        val currentHeight = storedHeight[player] ?: Height.HEIGHT_10

        val location = event.blockPlaced.location
        val world = location.world
        var cancelled = false
        event.isCancelled = true
        for (i in 0..currentHeight.height) {
            runLater(i + 1L) {
                if(cancelled) {
                    it.cancel()
                    return@runLater
                }
                val newLocation = location.clone().add(0.0, i.toDouble(), 0.0)
                val block = world.getBlockAt(newLocation)
                if(!block.isEmpty) {
                    cancelled = true
                    return@runLater
                }
                world.getBlockAt(newLocation).type = Material.RED_WOOL
                runLater(2L) {
                    world.getBlockAt(newLocation).type = Material.BARRIER
                }
            }
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player

        if(player.inventory.itemInMainHand != superBarrierToolItem) return
        val currentHeight = storedHeight[player] ?: Height.HEIGHT_10

        val location = event.block.location
        val world = location.world

        for (i in 0..currentHeight.height) {
            runLater(i + 2L) {
                val newLocation = location.clone().add(0.0, i.toDouble(), 0.0)
                val block = world.getBlockAt(newLocation).type
                if(block != Material.BARRIER) return@runLater

                world.getBlockAt(newLocation).type = Material.AIR
            }
        }
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if(event.itemDrop.itemStack != superBarrierToolItem) return

        val currentHeight = storedHeight[player] ?: Height.HEIGHT_10
        storedHeight[player] = nextEntry(currentHeight)
        player.playChangeSound()
        event.isCancelled = true
    }

    private enum class Height(val height: Int) {
        HEIGHT_5(5),
        HEIGHT_10(10),
        HEIGHT_20(20),
        HEIGHT_30(30),
        HEIGHT_50(50),
        HEIGHT_100(100),
    }
}