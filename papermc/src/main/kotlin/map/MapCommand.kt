package map

import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import selection.SelectionManager
import sendPrefixed
import util.error

class MapCommand {

    val cm = ShulkerboxPaper.instance.commandManager

    init {
        val mapCommandBase = cm.commandBuilder("map")
        val boundCommandBase = cm.commandBuilder("bound")
        val pointCommandBase = cm.commandBuilder("point")

        cm.command(mapCommandBase.literal("select")
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

        cm.command(mapCommandBase.literal("create")
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

        cm.command(mapCommandBase.literal("unselect")
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

                if(map.bounds.containsKey(id)) {
                    error(player, "Bound with id <dark_red>$id <red>already exists! If you want to redefine the bound use <yellow>/bound redefine <id>")
                    return@handler
                }

                val selection = SelectionManager.selectionMap[player]
                if(selection == null) {
                    error(player, "<red>You do not have any selection! Make a selection first!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.addBound(id, selection)
            }
        )

        cm.command(boundCommandBase.literal("redefine")
            .required("id", stringParser())
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                val id = ctx.get<String>("id")

                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                if(map.bounds[id] == null) {
                    error(player, "Bound with id <dark_red>$id <red>does not exist!")
                    return@handler
                }

                val selection = SelectionManager.selectionMap[player]
                if(selection == null) {
                    error(player, "<red>You do not have any selection! Make a selection first!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                val bound = map.bounds[id]!!
                bound.origin = selection.basePoint
                bound.size = selection.getBoundingBoxSize()
                activeMap.updateDrawables()
                player.sendPrefixed("<green>Successfully redefined bound with id <dark_green>$id")
            }
        )

        cm.command(boundCommandBase.literal("remove")
            .required("id", stringParser())
            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val map = MapManager.selectedShulkerboxMap(player)
                val id = ctx.get<String>("id")

                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                if(map.bounds[id] == null) {
                    error(player, "Bound with id <dark_red>$id <red>does not exist!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.removeBound(id)
                player.sendPrefixed("<red>Successfully removed bound with id <dark_red>$id")
            }
        )

        cm.command(pointCommandBase.literal("create")
            .required("id", stringParser())
            .required("type", enumParser(PointType::class.java))

            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val id = ctx.get<String>("id")
                val type = ctx.get<PointType>("type")
                val map = MapManager.selectedShulkerboxMap(player)

                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                val point = Point(id, player.location.clone().apply { yaw = player.yaw; pitch = 0f }, player.location.yaw, 0f, type)
                activeMap.addPoint(point)
            }
        )
        cm.command(boundCommandBase)
        cm.command(mapCommandBase)
    }
}