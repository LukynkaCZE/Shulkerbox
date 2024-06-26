package selection

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import send

class SelectionCommands {

    val cm = ShulkerboxPaper.instance.commandManager

    init {
        val mapCommand = cm.commandBuilder("selection")
        cm.command(mapCommand
            .permission("shulkerbox.map")
            .required("action", enumParser(SelectionCommandArg::class.java))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val action = ctx.get<SelectionCommandArg>("action")
                val selection = SelectionManager.selectionMap[player] ?: return@handler

                if(action == SelectionCommandArg.CLEAR) {
                    SelectionManager.remove(player)
                    player.send("${SelectionManager.prefix} Cleared your selection!")
                    player.playSound(player.location, Sound.ITEM_BUNDLE_REMOVE_ONE, 1.3f, 0.5f)
                }
            }
        )
    }

    enum class SelectionCommandArg {
        COLOR,
        NAME,
        CLEAR
    }
}