import org.bukkit.entity.Player
import selection.SelectionManager

class MapCommand() {

    val cm = ShulkerboxPaper.instance.commandManager

    init {
        val mapCommand = cm.commandBuilder("map")
        val itemCommand = cm.commandBuilder("selecttool")
        cm.command(mapCommand
            .permission("shulkerbox.map")
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                player.send("<yellow>yo")
            }
        )

        cm.command(itemCommand
            .permission("shulkerbox.map")
            .handler { ctx ->
                val player = ctx.sender() as Player
                player.inventory.setItemInMainHand(SelectionManager.selectionItem)
            }
        )
    }
}