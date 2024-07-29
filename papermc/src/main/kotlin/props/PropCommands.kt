package props

import ShulkerboxPaper
import map.*
import map.commands.giveItemSound
import map.commands.playEditSound
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
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

    init {
        val cm = ShulkerboxPaper.instance.commandManager
        val propCommandBase = cm.commandBuilder("prop")

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
                    location = location.toShulkerboxOffset(map,).toShulkerboxVector(),
                    yaw = 0f,
                    pitch = 0f,
                    meta = mutableMapOf(),
                    transformation = ShulkerboxTranform(),
                    brightness = 0,
                    itemStack = ItemStack(Material.GLASS).toPropItemStack()
                )

                player.sendPrefixed("<green>Created new prop!")
                activeMap.addProp(prop)
                activeMap.updateDrawables()
                PropManager.select(player, prop)
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
                PropManager.select(player, newProp)
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
                    PropManager.select(player, prop.prop)
                    player.sendPrefixed("<green>Selected a prop <aqua>${prop.prop.uid}<green>!")
                    prop.entity.isGlowing = true
                    prop.entity.glowColorOverride = Color.YELLOW
                    runLaterAsync(20) {
                        prop.entity.isGlowing = false
                    }
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

        if(ShulkerboxPaper.youkaiSupport) {
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

        cm.command(propCommandBase.literal("drag")
            .handler {ctx ->
                val player = ctx.sender() as Player
                player.inventory.addItem(PropManager.moveItem)
                player.sendPrefixed("You have been given the <aqua>1x Prop Move Tool<gray>!")
                player.giveItemSound()
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