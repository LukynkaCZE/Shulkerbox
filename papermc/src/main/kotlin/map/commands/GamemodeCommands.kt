package map.commands

import ShulkerboxPaper
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.parser.PlayerParser.playerParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import sendPrefixed

class GamemodeCommands {

    val manager = ShulkerboxPaper.instance.commandManager
    val commandBase = manager.commandBuilder("gamemode")
    val short = mapOf(
        "gmc" to GameMode.CREATIVE,
        "gms" to GameMode.SURVIVAL,
        "gma" to GameMode.ADVENTURE,
        "gmsp" to GameMode.SPECTATOR,
    )

    init {
        manager.command(commandBase
            .permission("shulkerbox.commands.admin")
            .required("gamemode", enumParser(GameMode::class.java))
            .optional("player", playerParser())
            .handler {ctx ->
                val player = ctx.getOrDefault<Player>("player", null) ?: ctx.getPlayerOrThrow()
                val gamemode = ctx.get<GameMode>("gamemode")
                player.gameMode = gamemode
                player.sendPrefixed("<gray>Set gamemode of <aqua>${player.name}<gray> to <yellow>${gamemode.name.toProperCase()}")
            }
        )

        short.forEach {
            val command = manager.commandBuilder(it.key)
            manager.command(command
                .permission("shulkerbox.commands.admin")
                .optional("player", playerParser())
                .handler {ctx ->
                    val player = ctx.getOrDefault<Player>("player", null) ?: ctx.getPlayerOrThrow()
                    val gamemode = it.value
                    player.gameMode = gamemode
                    player.sendPrefixed("<gray>Set gamemode of <aqua>${player.name}<gray> to <yellow>${gamemode.name.toProperCase()}")
                }
            )
        }
    }
}

fun CommandContext<CommandSender>.getPlayerOrThrow(): Player {
    if(sender() !is Player) {
        sender().sendMessage("Only players can execute this command!")
    }
    return (sender() as Player)
}

fun String.toProperCase(): String {
    return this.substring(0, 1).uppercase() + this.substring(1).lowercase();
}