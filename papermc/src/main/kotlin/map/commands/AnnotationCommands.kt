package map.commands

import ShulkerboxAnnotation
import ShulkerboxPaper
import map.MapManager
import map.toShulkerboxOffset
import map.toShulkerboxVector
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import props.PropManager
import sendPrefixed
import util.generateUid
import util.simpleSuggestion

class AnnotationCommands {

    private fun getAnnotationIdSuggestions(): BlockingSuggestionProvider.Strings<CommandSender> {
        return BlockingSuggestionProvider.Strings { commandContext, input ->
            MapManager.selectedShulkerboxMap(
                commandContext.sender() as Player
            )?.annotations!!.keys
        }
    }

    init {
        val cm = ShulkerboxPaper.instance.commandManager
        val annotationCommandBase = cm.commandBuilder("annotation")

        cm.command(annotationCommandBase.literal("create")
            .required("text", greedyStringParser(), simpleSuggestion("<text>"))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val text = ctx.get<String>("text")
                val map = MapManager.selectedShulkerboxMap(player)


                if (map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                val uid = generateUid(map)
                val annotation = ShulkerboxAnnotation(uid, player.location.toShulkerboxOffset(map).toShulkerboxVector(), text)
                activeMap.addAnnotation(annotation)
            }
        )

        cm.command(annotationCommandBase.literal("remove")
            .required("uid", stringParser(), getAnnotationIdSuggestions())
            .handler { ctx ->
                val player = ctx.sender() as Player
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }
                val annotation = map.annotations?.get(ctx.get<String>("uid"))
                if(annotation == null) {
                    util.error(player, "Annotation with that uid does not exist!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                map.annotations?.remove(annotation.uid)
                activeMap.updateDrawables()
            }
        )
    }
}