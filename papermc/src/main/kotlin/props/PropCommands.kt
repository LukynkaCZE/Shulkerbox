package props

import cz.lukynka.shulkerbox.common.Prop
import ShulkerboxPaper
import cz.lukynka.shulkerbox.common.ShulkerboxTranform
import map.*
import map.commands.*
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.incendo.cloud.parser.standard.DoubleParser.doubleParser
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import org.incendo.cloud.parser.standard.FloatParser.floatParser
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.joml.Vector3f
import sendPrefixed
import util.*
import youkai.YoukaiIntegration

class PropCommands {


    private fun getYoukaiModelSuggestions(): BlockingSuggestionProvider.Strings<CommandSender> {
        return BlockingSuggestionProvider.Strings { commandContext, input -> YoukaiIntegration.models.keys }
    }

    enum class AnchorFlipDirection {
        NORTH,
        EAST,
        SOUTH,
        WEST,
        UP,
        DOWN
    }

    init {
        val cm = ShulkerboxPaper.instance.commandManager
        val propCommandBase = cm.commandBuilder("prop")

        val propAxisCommandBase = propCommandBase.literal("anchor")

        cm.command(propAxisCommandBase.literal("flip")
            .required("direction", enumParser(AnchorFlipDirection::class.java))
            .handler { ctx ->
                val player = ctx.getPlayerOrThrow()
                val direction = ctx.get<AnchorFlipDirection>("direction")
                val map = MapManager.selectedShulkerboxMap(player)
                val prop = PropManager.propSelections[player]

                val anchorLocation = PropAxisTool.anchors[player]

                if(anchorLocation == null) {
                    error(player, "You do not have anchor placed")
                    return@handler
                }

                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                val bukkitLocation = map.origin!!.toBukkitLocation().clone().add(prop.location.toBukkitVector())

                val offset = bukkitLocation.clone().subtract(anchorLocation) // Get vector from prop to anchor
                val newLocation = when (direction) {
                    AnchorFlipDirection.NORTH -> {
                        anchorLocation.clone().add(offset.x, offset.y, -offset.z) // Flip offset on Z and add to anchor
                    }
                    AnchorFlipDirection.EAST -> {
                        anchorLocation.clone().add(-offset.z, offset.y, offset.x) // Flip offset on X and Z and add to anchor
                    }
                    AnchorFlipDirection.SOUTH -> {
                        anchorLocation.clone().add(offset.x, offset.y, offset.z) // Flip offset on Z and add to anchor (no actual change)
                    }
                    AnchorFlipDirection.WEST -> {
                        anchorLocation.clone().add(offset.z, offset.y, -offset.x) // Flip offset on X and Z and add to anchor
                    }
                    AnchorFlipDirection.UP -> {
                        anchorLocation.clone().add(offset.x, -offset.y, offset.z) // Flip offset on Y and add to anchor
                    }
                    AnchorFlipDirection.DOWN -> {
                        anchorLocation.clone().add(offset.x, offset.y, -offset.z) // Flip offset on Y and add to anchor
                    }
                }
                prop.location = newLocation.toShulkerboxOffset(map).toShulkerboxVector()
                activeMap.updateDrawables()
                player.playEditSound()
                player.sendPrefixed("Flipped prop on ${direction.name.toProperCase()} axis!")
            }
        )

        cm.command(propCommandBase.literal("create")
            .handler { ctx ->
                val player = ctx.sender() as Player
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                val location = player.location.apply { pitch = 0f; yaw = 0f }.add(0.0, 0.5, 0.0)
                val prop = Prop(
                    uid = generateUid(map),
                    location = location.toShulkerboxOffset(map).toShulkerboxVector(),
                    yaw = 0f,
                    pitch = 0f,
                    meta = mutableMapOf(),
                    transformation = ShulkerboxTranform(),
                    brightness = null,
                    itemStack = ItemStack(Material.GLASS).toPropItemStack()
                )

                player.sendPrefixed("<green>Created new prop!")
                activeMap.addProp(prop)
                activeMap.updateDrawables()
                val propEntity = activeMap.drawableProps.first { it.prop == prop }
                PropManager.select(player, propEntity)
            }
        )

        cm.command(propCommandBase.literal("brightness")
            .required("type", enumParser(PropBrightnessCommandType::class.java))
            .optional("brightness", integerParser(), simpleSuggestion("<brightness>"))
            .handler{ ctx ->
                val player = ctx.sender() as Player
                val type = ctx.get<PropBrightnessCommandType>("type")
                var brightness = ctx.getOrDefault<Int>("brightness", null)
                val map = MapManager.selectedShulkerboxMap(player)
                val prop = PropManager.propSelections[player]
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!
                when(type) {
                    PropBrightnessCommandType.SET -> {
                        if(brightness == null) {
                            error(player, "Field brightness must be set!")
                            return@handler
                        }

                        if(brightness > 15) brightness = 15
                        if(brightness < 0) brightness = 0
                        prop.brightness = brightness
                        player.sendPrefixed("Set brightness of prop to <yellow>$brightness")
                        player.playEditSound()
                    }
                    PropBrightnessCommandType.RESET -> {
                        prop.brightness = null
                        player.sendPrefixed("Reset brightness of prop!")
                        player.playEditSound()
                    }
                }
                activeMap.updateDrawables()
            }
        )

        cm.command(propCommandBase.literal("shift")
            .required("x", floatParser())
            .required("y", floatParser())
            .required("z", floatParser())
            .handler { ctx ->
                val player = ctx.sender() as Player

                val x = ctx.get<Float>("x")
                val y = ctx.get<Float>("y")
                val z = ctx.get<Float>("z")

                val map = MapManager.selectedShulkerboxMap(player)
                val prop = PropManager.propSelections[player]
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                prop.location = prop.location.toBukkitVector().add(Vector(x, y, z)).toShulkerboxVector()
                activeMap.updateDrawables()
            }
        )

        cm.command(propCommandBase.literal("shift_all")
            .required("x", floatParser())
            .required("y", floatParser())
            .required("z", floatParser())
            .handler { ctx ->
                val player = ctx.sender() as Player

                val x = ctx.get<Float>("x")
                val y = ctx.get<Float>("y")
                val z = ctx.get<Float>("z")

                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                map.props.forEach { prop ->
                    prop.value.location = prop.value.location.toBukkitVector().add(Vector(x, y, z)).toShulkerboxVector()
                }
                activeMap.updateDrawables()
            }
        )

        cm.command(propCommandBase.literal("remove")
            .handler { ctx ->
                val player = ctx.sender() as Player
                val map = MapManager.selectedShulkerboxMap(player)
                val prop = PropManager.propSelections[player]
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                PropManager.unselect(player)
                map.props.remove(prop.uid)
                player.sendPrefixed("<red>Removed a prop!")
                activeMap.updateDrawables()
            }
        )

        cm.command(propCommandBase.literal("clone")
            .handler { ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                val newProp = prop.copy().apply { uid = generateUid(map) }
                PropManager.unselect(player)
                activeMap.addProp(newProp)
                activeMap.updateDrawables()
                player.sendPrefixed("<yellow>Cloned a prop!")
                val propEntity = activeMap.drawableProps.first { it.prop == newProp }
                PropManager.select(player, propEntity)
            }
        )

        cm.command(propCommandBase.literal("unselect")
            .handler { ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                PropManager.unselect(player)
                player.sendPrefixed("<red>Unselected a prop!")
            }
        )

        cm.command(propCommandBase.literal("select")
            .handler { ctx ->
                val player = ctx.sender() as Player
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                val props = activeMap.drawableProps.sortedBy { it.location.distance(player.location) }
                props.forEach { prop ->
                    if(PropManager.propSelections[player] != null && PropManager.propSelections[player]!!.uid == prop.prop.uid) return@forEach
                    PropManager.select(player, prop)
                    return@handler
                }
            }
        )

        cm.command(propCommandBase.literal("size")
            .required("x", floatParser(), getCurrentPropSizeSuggestion("x"))
            .optional("y", floatParser(), getCurrentPropSizeSuggestion("y"))
            .optional("z", floatParser(), getCurrentPropSizeSuggestion("z"))
            .handler {ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                val x = ctx.get<Float>("x")
                val y = ctx.getOrDefault<Float>("y", x)
                val z = ctx.getOrDefault<Float>("z", x)
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                val current = prop.transformation.toTransformation()
                val newTransformation = Transformation(current.translation, current.leftRotation, Vector3f(x, y, z), current.rightRotation)
                prop.transformation = newTransformation.toShulkerboxTranform()
                activeMap.updateDrawables()

                player.sendPrefixed("Set scale transform to <green>${prop.transformation.scale}<yellow>!")
                player.playEditSound()
            })

        cm.command(propCommandBase.literal("snap_rotation")
            .optional("increment", doubleParser(), simpleSuggestion("<snap increment>"))
            .handler {ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                val snapIncrement = ctx.getOrDefault<Double>("increment", 25.0)
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                val tranform = prop.transformation.toTransformation()
                val newRightRotation = snapRotationToAxis(tranform.rightRotation, snapIncrement)
                val newTranform = Transformation(tranform.translation, tranform.leftRotation, tranform.scale, newRightRotation)
                prop.transformation = newTranform.toShulkerboxTranform()
                activeMap.updateDrawables()
                player.playEditSound()
            })

        cm.command(propCommandBase.literal("item")
            .handler {ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                var item = player.inventory.itemInMainHand
                if(item.type == Material.AIR) item = ItemStack(Material.GLASS)
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!
                prop.youkaiModelId = null

                prop.itemStack = item.toPropItemStack()
                player.sendPrefixed("Set the item of the prop to <green>${item.type.name}<gray>!")
                player.playEditSound()
                activeMap.updateDrawables()
            })

        if(ShulkerboxPaper.youkaiIntegration) {
            cm.command(propCommandBase.literal("youkai")
                .required("id", stringParser(), getYoukaiModelSuggestions())
                .handler {ctx ->
                    val player = ctx.sender() as Player
                    val prop = PropManager.propSelections[player]
                    val id = ctx.get<String>("id")
                    val item = YoukaiIntegration.getModel(id)
                    if(prop == null) {
                        error(player, "You do not have any prop selected")
                        return@handler
                    }
                    val map = MapManager.selectedShulkerboxMap(player)
                    if(map == null) {
                        error(player, "You don't have any map selected!")
                        return@handler
                    }
                    val activeMap = MapManager.mapSelections[player]!!

                    prop.itemStack = item.toPropItemStack()
                    prop.youkaiModelId = id
                    player.sendPrefixed("Set the item of the prop to <green>${item.type.name}<gray>!")
                    player.playEditSound()
                    activeMap.updateDrawables()
                })
        }

        cm.command(propCommandBase.literal("tool")
            .handler {ctx ->
                val player = ctx.sender() as Player
                player.inventory.addItem(PropManager.propMoveToolItem)
                player.sendPrefixed("You have been given the <aqua>1x Prop Move Tool<gray>!")
                player.giveItemSound()
            })

        cm.command(propCommandBase.literal("copy_from_map")
            .required("map_id", stringParser(), MapCommand.getMapIdSuggestions())
            .handler {ctx ->
                val player = ctx.sender() as Player
                val mapId = ctx.get<String>("map_id")
                val map = MapManager.maps[mapId]
                if(map == null) {
                    error(player, "Map with the id <dark_red>$mapId <red>does not exist!")
                    return@handler
                }

                val currentMap = MapManager.mapSelections[player]
                if(currentMap == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }

                map.props.forEach {
                    val newUid = generateUid(currentMap.map)
                    val newProp = it.value.copy().apply { uid = newUid }
                    currentMap.map.props[newUid] = newProp
                }
                currentMap.updateDrawables()

                player.playEditSound()
                player.sendPrefixed("<lime>Successfully copied <yellow>${map.props.size} props<gray> to your current selected map!")
            })

        cm.command(propCommandBase.literal("tp")
            .optional("arg", stringParser(), simpleSuggestion("-keepRot", "-noRot", "-keepYaw", "-keepPitch"))
            .handler {ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                val location = player.location
                val arg = ctx.getOrDefault<String>("arg", null)
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                val map = MapManager.selectedShulkerboxMap(player)
                if(map == null) {
                    error(player, "You don't have any map selected!")
                    return@handler
                }
                val activeMap = MapManager.mapSelections[player]!!

                if(arg == "-keepRot") {
                    location.pitch = prop.pitch
                    location.yaw = prop.yaw
                }
                if(arg == "-noRot") {
                    location.pitch = 0f
                    location.yaw = 0f
                }

                if(arg == "-keepYaw") {
                    location.pitch = prop.pitch
                }

                if(arg == "-keepPitch") {
                    location.yaw = prop.yaw
                }

                activeMap.updateDrawables()
                player.sendPrefixed("Teleported prop to <green>${location.toXYZString()}<yellow>!")
                player.playEditSound()
            })

    }

    enum class PropBrightnessCommandType {
        SET,
        RESET
    }

    private fun getCurrentPropSizeSuggestion(face: String): BlockingSuggestionProvider.Strings<CommandSender> {
        return BlockingSuggestionProvider.Strings { commandContext, input ->
            val player = commandContext.sender() as Player
            val out = mutableListOf<String>()
            val itemDisplay = PropManager.propSelections[player]
            if(itemDisplay != null) {
                val scale = when(face) {
                    "x" -> itemDisplay.transformation.scale.x
                    "z" -> itemDisplay.transformation.scale.y
                    "y" -> itemDisplay.transformation.scale.z
                    else -> 0f
                }
                out.add("$scale")
            }
            out
        }
    }
}