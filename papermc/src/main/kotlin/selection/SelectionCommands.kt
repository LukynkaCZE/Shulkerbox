package selection

import BoundingBoxColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.parser.PlayerParser.playerParser
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import send
import sendPrefixed

@Suppress("UnstableApiUsage")
class SelectionCommands {

    val cm = ShulkerboxPaper.instance.commandManager

    init {
        val commandBase = cm.commandBuilder("selection")
        commandBase.permission("shulkerbox.use")

        cm.command(commandBase.literal("clear")
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                SelectionManager.remove(player)
                player.send("${SelectionManager.prefix} <red>Cleared your selection!")
                player.playSound(player.location, Sound.ITEM_BUNDLE_REMOVE_ONE, 1.3f, 0.5f)
            })

        cm.command(commandBase.literal("color")
            .required("color", enumParser(BoundingBoxColor::class.java))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val color = ctx.get<BoundingBoxColor>("color")
                val selection = SelectionManager.selectionMap[player]
                if(selection == null) {
                    util.error(player, "You don't have a selection!")
                    return@handler
                }

                selection.boundingBoxEntity.setColor(color)
                player.send("${SelectionManager.prefix} set the color of your selection to <${getBoundingBoxColorData(color).textColor.asHexString()}>${color.name}")
                player.playSound(player.location, Sound.ITEM_BUCKET_FILL, 0.5f, 1.5f)
            })

        cm.command(commandBase.literal("tool")
            .optional("player", playerParser())
            .handler { ctx ->
                val senderPlayer = (ctx.sender() as Player)
                val player = ctx.getOrDefault<Player>("player", ctx.sender() as Player)
                player.inventory.addItem(SelectionManager.selectionToolItem)
                val message = if(player == senderPlayer) "You have been given the <green>Selection tool" else "You have been given the <green>Selection tool <gray>by <aqua>${senderPlayer.name}<gray>!"
                player.sendPrefixed(message)
                player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1.3f, 2f)
            }
        )
        cm.command(commandBase)
    }
}