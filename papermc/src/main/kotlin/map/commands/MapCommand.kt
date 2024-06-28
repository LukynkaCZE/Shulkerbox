package map.commands

import map.MapManager
import map.ShulkerboxMap
import map.toShulkerboxVector
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import selection.SelectionManager
import send
import sendPrefixed
import util.error
import util.simpleSuggestion

class MapCommand {

    val cm = ShulkerboxPaper.instance.commandManager
    val suggestions = MapManager.maps.keys.map { Suggestion.suggestion(it) }.toMutableList()

    init {
        val mapCommandBase = cm.commandBuilder("map")

        cm.command(mapCommandBase.literal("select")
            .required("map_id", stringParser(), SuggestionProvider.suggesting(suggestions))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val mapId = ctx.get<String>("map_id")
                val map = MapManager.maps[mapId]
                if(map == null) {
                    error(player, "Map with the id <dark_red>$mapId <red>does not exist!")
                    return@handler
                }

                MapManager.select(player, map)
            })

        cm.command(mapCommandBase.literal("create")
            .required("map_id", stringParser(), SuggestionProvider.suggesting(Suggestion.suggestion("<map id>")))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val mapId = ctx.get<String>("map_id")
                if(MapManager.maps.containsKey(mapId)) {
                    error(player, "Map with the id <dark_red>$mapId <red>already exists!")
                    return@handler
                }

                val selection = SelectionManager.selectionMap[player]
                if(selection == null) {
                    error(player, "<red>You do not have any selection! Make a selection containing the map first")
                    return@handler
                }

                val map = ShulkerboxMap(mapId, origin = selection.basePoint, size = selection.getBoundingBoxSize().toShulkerboxVector())
                MapManager.maps[mapId] = map
                MapManager.select(player, map)

                player.sendPrefixed("<green>Successfully created map <yellow>${map.id}<green>!")
                player.playEditSound()
                SelectionManager.remove(player)
            })

        cm.command(mapCommandBase.literal("unselect")
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                if(!MapManager.hasMapSelected(player)) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                MapManager.unselect(player)
            })

        cm.command(mapCommandBase.literal("redefine")
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                val selection = SelectionManager.selectionMap[player]
                if(selection == null) {
                    error(player, "<red>You do not have any selection! Make a selection containing the map first")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                map.origin = selection.basePoint
                map.size = selection.getBoundingBoxSize().toShulkerboxVector()
                activeMap.updateDrawables()

                player.sendPrefixed("<green>Successfully redefined size of map <yellow>${map.id}<green>!")
                player.playEditSound()
                SelectionManager.remove(player)
            })


        cm.command(mapCommandBase.literal("name")
            .required("name", stringParser(), SuggestionProvider.suggesting(Suggestion.suggestion("<name>")))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                val name = ctx.get<String>("name")

                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                map.name = name
                player.sendPrefixed("<green>Successfully changed display name of map <yellow>${map.id} <green>to <aqua>${map.name}")
                player.playEditSound()

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.updateDrawables()
            })

        cm.command(mapCommandBase.literal("meta")
            .required("action", enumParser(ShulkerboxMetaAction::class.java))
            .optional("key", stringParser(), SuggestionProvider.suggesting(Suggestion.suggestion("<key>")))
            .optional("value", stringParser(), SuggestionProvider.suggesting(Suggestion.suggestion("<value>")))

            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)

                val action: ShulkerboxMetaAction = ctx.get<ShulkerboxMetaAction>("action")
                val key: String? = ctx.getOrDefault<String>("key", null)
                val value: String? = ctx.getOrDefault<String>("value", null)

                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                when(action) {
                    ShulkerboxMetaAction.EDIT,
                    ShulkerboxMetaAction.ADD -> {
                        if(value == null || key == null) {
                            error(player, "<key> and <value> arguments are required for this action!")
                            return@handler
                        }

                        map.meta[key] = value
                        player.sendPrefixed("Set metadata tag with key <yellow>$key <gray>to <aqua>$value")
                        player.playEditSound()
                    }
                    ShulkerboxMetaAction.REMOVE -> {
                        if(key == null) {
                            error(player, "<key> argument is required for this action!")
                            return@handler
                        }
                        if(map.meta[key] == null) {
                            error(player, "Metadata tag with key <dark_red>$key <red>does not exist in map <dark_red>${map.id}<red>!")
                            return@handler
                        }

                        map.meta.remove(key)
                        player.sendPrefixed("Removed metadata tag with key <yellow>$key <gray>from map metadata!")
                        player.playEditSound()
                    }
                    ShulkerboxMetaAction.CLEAR_ALL -> {
                        map.meta.clear()
                        player.sendPrefixed("Cleared all metadata tags from the map metadata!")
                        player.playEditSound()
                    }

                    ShulkerboxMetaAction.GET -> {
                        player.send(" ")
                        player.sendPrefixed("Metadata tags of map <yellow>${map.id}<gray>:")
                        map.meta.forEach {
                            player.send("   <dark_gray>- <green>${it.key} <gray>= <aqua>${it.value}")
                        }
                        player.send(" ")
                    }
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.updateDrawables()
            })


        cm.command(mapCommandBase.literal("remove")
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)

                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                player.sendPrefixed("<red>Successfully removed map <dark_red>${map.id}<red>!")
                player.playEditSound()

                MapManager.unselect(player)
                MapManager.maps.remove(map.id)
            })

        cm.command(mapCommandBase.literal("save")
            .optional("push", stringParser(), simpleSuggestion("push"))
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                val push = ctx.getOrDefault<String>("push", null)

                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                player.sendPrefixed("<yellow>Saving map.. this could lag the server!")
                try {
                    MapManager.save(map)
                } catch (ex: Exception) {
                    error(player, "Map saving failed: $ex")
                    ex.printStackTrace()
                    return@handler
                }

                player.sendPrefixed("<green>Successfully saved map <yellow>${map.id}<green> to <aqua>Shulkerbox/${map.id}/map.shulker<green>!")
                player.playEditSound()
            })
        cm.command(mapCommandBase)
    }
}

enum class ShulkerboxMetaAction {
    GET,
    ADD,
    EDIT,
    REMOVE,
    CLEAR_ALL
}

fun Player.playEditSound() {
    this.playSound(this.location, Sound.UI_LOOM_TAKE_RESULT, 1f, 1f)
}