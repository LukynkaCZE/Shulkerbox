package map

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.Shulker
import org.incendo.cloud.parser.standard.StringParser.stringParser
import selection.SelectionManager
import send
import util.error

class MapCommand {

    val cm = ShulkerboxPaper.instance.commandManager

    init {
        val commandBase = cm.commandBuilder("map")
        val boundCommandBase = cm.commandBuilder("bound")

        cm.command(commandBase.literal("select")
            .required("map_id", stringParser())
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

        cm.command(commandBase.literal("create")
            .required("map_id", stringParser())
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

                val map = ShulkerboxMap(mapId, origin = selection.basePoint, size = selection.getBoundingBoxSize())
                MapManager.maps[mapId] = map
                MapManager.select(player, map)
            })

        cm.command(commandBase.literal("unselect")
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                if(!MapManager.hasMapSelected(player)) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                MapManager.unselect(player)
            })

        cm.command(boundCommandBase.literal("create")
            .required("id", stringParser())
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val id = ctx.get<String>("id")
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

                if(map.bounds.containsKey(id)) {
                    error(player, "Bound with id <dark_red>$id <red>already exists in this map!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.addBound(id, selection)
            }
        )

        cm.command(boundCommandBase)
        cm.command(commandBase)
    }


}