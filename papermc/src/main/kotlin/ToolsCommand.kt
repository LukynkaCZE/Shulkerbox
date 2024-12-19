import map.commands.getPlayerOrThrow
import map.commands.giveItemSound
import map.commands.toProperCase
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import props.PropAxisTool
import props.PropManager
import selection.SelectionManager
import tools.SuperBarrierTool

class ToolsCommand {

    init {
        val cm = ShulkerboxPaper.instance.commandManager
        val pointCommandBase = cm.commandBuilder("tools")

        cm.command(pointCommandBase
            .optional("tool", enumParser(ShulkerboxTool::class.java))
            .handler { ctx ->
                val player = ctx.getPlayerOrThrow()
                val tool = ctx.getOrDefault<ShulkerboxTool>("tool", null)

                if(tool != null) {
                    val item = when(tool) {
                        ShulkerboxTool.SELECTION -> SelectionManager.selectionToolItem
                        ShulkerboxTool.PROP -> PropManager.propMoveToolItem
                        ShulkerboxTool.SUPER_BARRIER -> SuperBarrierTool.superBarrierToolItem
                        ShulkerboxTool.PROP_AXIS -> PropAxisTool.propAxisToolItem
                    }

                    player.inventory.addItem(item)
                    player.giveItemSound()
                    player.sendPrefixed("You have been given the <yellow>${tool.name.toProperCase()}<gray> tool!")
                } else {
                    ToolsScreen().open(player)
                }
            }
        )
    }

    enum class ShulkerboxTool {
        SELECTION,
        PROP,
        PROP_AXIS,
        SUPER_BARRIER,
    }
}