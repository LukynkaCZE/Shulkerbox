package map.commands

import map.*
import org.bukkit.entity.Player
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import sendPrefixed
import util.error
import util.generateUid
import util.simpleSuggestion

class PointCommands {

    val cm = ShulkerboxPaper.instance.commandManager

    init {
        val pointCommandBase = cm.commandBuilder("point")
        cm.command(pointCommandBase.literal("create")
            .required("id", stringParser(), simpleSuggestion("<id>"))
            .required("type", enumParser(PointType::class.java))

            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val id = ctx.get<String>("id")
                val type = ctx.get<PointType>("type")
                val map = MapManager.selectedShulkerboxMap(player)

                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }

                if(type == PointType.UNIQUE && map.points.values.firstOrNull { it.id == id } != null) {
                    error(player, "A Unique point with id <dark_red>$id <red>already exists on this map!")
                    return@handler
                }

                val activeMap = MapManager.mapSelections[player]!!
                val uid: String = generateUid(map)
                val point = Point(
                    id = id,
                    location = player.location.clone().apply { yaw = player.yaw; pitch = 0f }.toShulkerboxOffset(map).toShulkerboxVector(),
                    yaw = player.location.yaw,
                    pitch = 0f,
                    type = type,
                    meta = mutableMapOf(),
                    uid = uid
                )
                activeMap.addPoint(point)
            }
        )

        cm.command(pointCommandBase.literal("remove")
            .required("uid", stringParser(), simpleSuggestion("<uid>"))

            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val uid = ctx.get<String>("uid")
                val map = MapManager.selectedShulkerboxMap(player)

                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }

                val point = map.points[uid]
                if(point == null) {
                    util.error(player, "There are no points with the uid <dark_red>$uid")
                    return@handler
                }

                map.points.remove(uid)

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.updateDrawables()

                player.sendPrefixed("Removed point <red>${point.id} <dark_red>(${point.uid}) <gray>from the map!")
                player.playEditSound()
            }
        )

        cm.command(pointCommandBase.literal("redefine")
            .required("uid", stringParser(), simpleSuggestion("<uid>"))
            .optional("type", enumParser(PointType::class.java))

            .handler { ctx ->
                val player = (ctx.sender() as Player)
                val uid = ctx.get<String>("uid")
                val type = ctx.getOrDefault<PointType>("type", null)
                val map = MapManager.selectedShulkerboxMap(player)

                if(map == null) {
                    util.error(player, "You don't have any map selected!")
                    return@handler
                }

                val point = map.points[uid]
                if(point == null) {
                    util.error(player, "There are no points with the uid <dark_red>$uid")
                    return@handler
                }

                point.location = player.location.clone().apply { yaw = player.yaw; pitch = 0f }.toShulkerboxOffset(map).toShulkerboxVector()
                point.pitch = player.pitch
                point.yaw = 0f

                if(type != null) {
                    val existing = map.points.values.firstOrNull { it.type == type }
                    if(existing != null && type == PointType.UNIQUE) {
                        error(player, "A Unique point with id <dark_red>${point.id} <red>already exists on this map!")
                        return@handler
                    }
                    point.type = type
                }

                val activeMap = MapManager.mapSelections[player]!!
                activeMap.updateDrawables()

                player.sendPrefixed("Redefined point <yellow>${point.id} <gold>(${point.uid}) <gray>!")
                player.playEditSound()
            }
        )
    }
}