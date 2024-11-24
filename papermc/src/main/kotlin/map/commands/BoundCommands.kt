package map.commands

import map.MapManager
import map.toShulkerboxOffset
import map.toShulkerboxVector
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import selection.SelectionManager
import send
import sendPrefixed
import util.simpleSuggestion

class BoundCommands {

    private fun getBoundIdSuggestions(): BlockingSuggestionProvider.Strings<CommandSender> {
        return BlockingSuggestionProvider.Strings { commandContext, input -> MapManager.selectedShulkerboxMap(commandContext.sender() as Player)?.bounds!!.keys }
    }

    init {
        val cm = ShulkerboxPaper.instance.commandManager
        val boundCommandBase = cm.commandBuilder("bound")

        cm.command(boundCommandBase.literal("create")
            .required("id", stringParser(), simpleSuggestion("<id>"))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val id = ctx.get<String>("id")
                val map = MapManager.selectedShulkerboxMap(player)

                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }

                if(map.bounds.containsKey(id)) {
                    util.error(
                        player,
                        "Bound with id <dark_red>$id <red>already exists! If you want to redefine the bound use <yellow>/bound redefine <id>"
                    )
                    return@handler
                }

                val selection = SelectionManager.selectionMap[player]
                if(selection == null) {
                    util.error(player, "<red>You do not have any selection! Make a selection first!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.addBound(id, selection)
                SelectionManager.remove(player)
                player.sendPrefixed("<green>Successfully created bound with id <yellow>$id<green>!")
                player.playEditSound()
            }
        )

        cm.command(boundCommandBase.literal("redefine")
            .required("id", stringParser(), getBoundIdSuggestions())
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                val id = ctx.get<String>("id")

                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }

                if(map.bounds[id] == null) {
                    util.error(player, "Bound with id <dark_red>$id <red>does not exist!")
                    return@handler
                }

                val selection = SelectionManager.selectionMap[player]
                if(selection == null) {
                    util.error(player, "<red>You do not have any selection! Make a selection first!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                val bound = map.bounds[id]!!
                bound.origin = selection.getFirstPoint().toShulkerboxOffset(map).toShulkerboxVector()
                bound.size = selection.getBoundingBoxSize().toShulkerboxVector()
                activeMap.updateDrawables()
                player.sendPrefixed("<green>Successfully redefined the size of bound with id <yellow>$id<green>!")
                player.playEditSound()
                SelectionManager.remove(player)
            }
        )

        cm.command(boundCommandBase.literal("remove")
            .required("id", stringParser(), getBoundIdSuggestions())
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                val id = ctx.get<String>("id")

                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }

                if(map.bounds[id] == null) {
                    util.error(player, "Bound with id <dark_red>$id <red>does not exist!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.removeBound(id)
                player.sendPrefixed("<red>Successfully removed bound with id <dark_red>$id")
                player.playEditSound()
                SelectionManager.remove(player)
            }
        )

        cm.command(boundCommandBase.literal("meta")
            .required("id", stringParser(), getBoundIdSuggestions())
            .required("action", enumParser(ShulkerboxMetaAction::class.java))
            .optional("key", stringParser(), SuggestionProvider.suggesting(Suggestion.suggestion("<key>")))
            .optional("value", stringParser(), SuggestionProvider.suggesting(Suggestion.suggestion("<value>")))

            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                val id = ctx.get<String>("id")

                val action: ShulkerboxMetaAction = ctx.get<ShulkerboxMetaAction>("action")
                val key: String? = ctx.getOrDefault<String>("key", null)
                val value: String? = ctx.getOrDefault<String>("value", null)

                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }

                val bound = map.bounds[id]
                Bukkit.broadcastMessage("${map.bounds}")
                if(bound == null) {
                    util.error(player, "There is no bound with the id <dark_red>$id")
                    return@handler
                }

                when(action) {
                    ShulkerboxMetaAction.EDIT,
                    ShulkerboxMetaAction.ADD -> {
                        if(value == null || key == null) {
                            util.error(player, "<key> and <value> arguments are required for this action!")
                            return@handler
                        }

                        bound.meta[key] = value
                        player.sendPrefixed("Set metadata tag with key <yellow>$key <gray>to <aqua>$value")
                        player.playEditSound()
                    }
                    ShulkerboxMetaAction.REMOVE -> {
                        if(key == null) {
                            util.error(player, "<key> argument is required for this action!")
                            return@handler
                        }
                        if(bound.meta[key] == null) {
                            util.error(
                                player,
                                "Metadata tag with key <dark_red>$key <red>does not exist in bound <dark_red>${bound.id}<red>!"
                            )
                            return@handler
                        }

                        bound.meta.remove(key)
                        player.sendPrefixed("Removed metadata tag with key <yellow>$key <gray>from bound metadata!")
                        player.playEditSound()
                    }
                    ShulkerboxMetaAction.CLEAR_ALL -> {
                        bound.meta.clear()
                        player.sendPrefixed("Cleared all metadata tags from the bound metadata!")
                        player.playEditSound()
                    }

                    ShulkerboxMetaAction.GET -> {
                        player.send(" ")
                        player.sendPrefixed("Metadata tags of bound <yellow>${bound.id}<gray>:")
                        bound.meta.forEach {
                            player.send("   <dark_gray>- <green>${it.key} <gray>= <aqua>${it.value}")
                        }
                        player.send(" ")
                    }
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.updateDrawables()
            })

        cm.command(boundCommandBase)
    }
}