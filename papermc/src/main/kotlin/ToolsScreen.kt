import com.mattmx.ktgui.components.button.GuiButton
import com.mattmx.ktgui.components.screen.GuiScreen
import com.mattmx.ktgui.dsl.button
import map.commands.giveItemSound
import map.commands.playTeleportSound
import map.commands.toProperCase
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import props.PropAxisTool
import props.PropManager
import selection.SelectionManager
import selection.SelectionManager.prefix
import tools.SuperBarrierTool

class ToolsScreen: GuiScreen("$prefix <black>Shulkerbox Tool Menu".toMiniMessage(), type = InventoryType.DROPPER, rows = 1) {

    init {
        ToolsCommand.ShulkerboxTool.entries.forEachIndexed { index, tool ->
            getItemFromTool(tool, index)
        }

        open { player ->
            player.playTeleportSound()
        }
    }

    private fun getItemFromTool(tool: ToolsCommand.ShulkerboxTool, slot: Int): GuiButton<out GuiButton<*>> {
        var description = "No description."
        var material = Material.BARRIER
        var item = ItemStack(Material.BARRIER, 1)
        when(tool) {
            ToolsCommand.ShulkerboxTool.SELECTION -> {
                description = "Allows you to create shulkerbox selection"
                material = Material.LEAD
                item = SelectionManager.selectionToolItem
            }
            ToolsCommand.ShulkerboxTool.PROP -> {
                description = "Allows you to move, rotate and scale a prop"
                material = Material.SPECTRAL_ARROW
                item = PropManager.propMoveToolItem
            }
            ToolsCommand.ShulkerboxTool.SUPER_BARRIER -> {
                description = "Creates variable size high barrier when you place it"
                material = Material.BARRIER
                item = SuperBarrierTool.superBarrierToolItem
            }
            ToolsCommand.ShulkerboxTool.PROP_AXIS -> {
                description = "Allows you to flip and rotate prop around set axis anchor"
                material = Material.LIGHTNING_ROD
                item = PropAxisTool.propAxisToolItem
            }
        }

        return button(material) {
            slot(slot)
            named("<green><u>${tool.name.toProperCase()} Tool".toMiniMessage())
            lore {
                add(" ".toMiniMessage())
                add("<gray>$description".toMiniMessage())
                add(" ".toMiniMessage())
                add("<yellow>◆ <gold>Left-Click<yellow> to get this tool".toMiniMessage())
            }
            click {
                ClickType.LEFT {
                    player.sendPrefixed("You have been given the <yellow>${tool.name.toProperCase()}<gray> tool!")
                    player.inventory.addItem(item)
                    player.giveItemSound()
                }
            }
        }
    }
}