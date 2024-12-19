package props

import BoundingBoxColor
import ShulkerboxPaper
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import fakes.FakeBlockDisplay
import fakes.FakeItemDisplay
import map.commands.playDestroySound
import map.commands.playSuccessSound
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.GlassPane
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Vector3f
import selection.SelectionManager.prefix
import selection.getBoundingBoxColorData
import sendPrefixed
import toMiniMessage

object PropAxisTool : Listener {
    val propAxisToolItem = ItemStack(Material.LIGHTNING_ROD)

    val anchors = mutableMapOf<Player, Location>()
    val anchorEntities = mutableMapOf<Player, FakeBlockDisplay>()
    val tempAnchorEntities = mutableMapOf<Player, FakeBlockDisplay>()

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
        propAxisToolItem.editMeta {
            it.displayName("<color:#ff0090><u>Prop Axis Tool".toMiniMessage())
            it.lore(
                mutableListOf(
                    " ".toMiniMessage(),
                    "<gray>Allows you to flip and rotate prop around set axis anchor".toMiniMessage(),
                    "<gray>Set the anchor by <aqua>Left Clicking <gray>for inside of block".toMiniMessage(),
                    "<gray>and <aqua>Right Clicking <gray>on the clicked block face".toMiniMessage(),
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
        Bukkit.getOnlinePlayers().forEach { player ->

            if(player.inventory.itemInMainHand != propAxisToolItem) {
                if(tempAnchorEntities[player] != null) {
                    val tempEntity = tempAnchorEntities[player]
                    tempEntity?.despawn()
                    tempAnchorEntities.remove(player)
                }
                return@forEach
            }

            val anchor = if(anchors[player] == null) "<red><bold>Not set" else "<green><bold>Set"
            player.sendActionBar("$prefix <yellow>Anchor: $anchor<reset> <dark_gray>| <yellow>Set anchor inside block: <gold>Left-Click <dark_gray>| <yellow>Set anchor on block face: <gold>Right-Click <dark_gray>| <yellow>Destroy anchor: <gold>Drop Item (<key:key.drop>)".toMiniMessage())

            if(anchors[player] != null) return@forEach
            val targetBlock = player.getTargetBlockExact(6) ?: return@forEach

            if(tempAnchorEntities[player] == null) {
                tempAnchorEntities[player] = createAnchorEntity(player, targetBlock.location, true)
            } else {
                val entity = tempAnchorEntities[player]!!
                entity.teleport(targetBlock.location)
            }
        }
    }

    @EventHandler
    fun blockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        if(player.inventory.itemInMainHand != propAxisToolItem) return

        event.isCancelled = true

        val anchorPoint = event.block.location
        createAnchor(player, anchorPoint)
    }

    @EventHandler
    fun blockPlaceEvent(event: BlockPlaceEvent) {
        val player = event.player
        if(player.inventory.itemInMainHand != propAxisToolItem) return

        event.isCancelled = true

        val anchorPoint = event.blockPlaced.location
        createAnchor(player, anchorPoint)
    }

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        val player = event.player
        if(event.itemDrop.itemStack != propAxisToolItem) return

        player.sendPrefixed("<red>Destroyed active axis anchor!")
        destroyCurrentAnchor(player)
        player.playDestroySound()
        event.isCancelled = true
    }

    private fun createAnchor(player: Player, location: Location) {
        destroyCurrentAnchor(player)

        anchors[player] = location.toCenterLocation()
        anchorEntities[player] = createAnchorEntity(player, location, false)

        player.playSuccessSound()
    }

    fun createAnchorEntity(player: Player, location: Location, temp: Boolean): FakeBlockDisplay {

        val anchorEntity = FakeBlockDisplay(location)
        val material = if(temp) Material.RED_STAINED_GLASS_PANE else Material.LIGHT_BLUE_STAINED_GLASS_PANE
        val blockState = material.createBlockData()

        val glassPane = blockState as GlassPane
        glassPane.setFace(BlockFace.NORTH, false)
        glassPane.setFace(BlockFace.EAST, false)
        glassPane.setFace(BlockFace.SOUTH, false)
        glassPane.setFace(BlockFace.WEST, false)
        glassPane.isWaterlogged = false

        anchorEntity.setBlock(glassPane.createBlockState())
        anchorEntity.setGlowing(true)
        anchorEntity.setGlowColor(if(temp) Color.RED else Color.AQUA)

        anchorEntity.addViewer(player)
        return anchorEntity
    }

    private fun destroyCurrentAnchor(player: Player) {
        val entity = anchorEntities[player]
        entity?.despawn()

        val tempEntity = tempAnchorEntities[player]
        tempEntity?.despawn()

        anchorEntities.remove(player)
        tempAnchorEntities.remove(player)
        anchors.remove(player)
    }

    @EventHandler
    fun leave(event: PlayerQuitEvent) {
        val player = event.player
        destroyCurrentAnchor(player)
    }
}