package essentials.commands

import ShulkerboxPaper
import essentials.setGamemode
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser.multiplePlayerSelectorParser

class GamemodeCommands() {

    var cm = ShulkerboxPaper.instance.commandManager

    init {
        val shortGamemodeCommands = mutableListOf<Pair<String, GameMode>>()
        shortGamemodeCommands.add(Pair("gmc", GameMode.CREATIVE))
        shortGamemodeCommands.add(Pair("gma", GameMode.ADVENTURE))
        shortGamemodeCommands.add(Pair("gms", GameMode.SURVIVAL))
        shortGamemodeCommands.add(Pair("gmsp", GameMode.SPECTATOR))

        shortGamemodeCommands.forEach { command ->
            val commandName = command.first
            val gamemode = command.second

            val commandHandler = cm.commandBuilder(commandName)
            cm.command(commandHandler
                .permission("cc.admin")
                .optional("players", multiplePlayerSelectorParser())
                .handler { ctx ->
                    val selector = ctx.getOrDefault<MultiplePlayerSelector?>("players", null)
                    var players = mutableListOf<Player>()

                    if(selector == null) {
                        players.add(ctx.sender() as Player)
                    } else {
                        players = selector.values().toMutableList()
                    }

                    players.setGamemode(gamemode)
                }
            )
        }
    }
}