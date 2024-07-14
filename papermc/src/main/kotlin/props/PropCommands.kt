package props

import ShulkerboxPaper
import map.commands.giveItemSound
import map.commands.playEditSound
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.incendo.cloud.parser.standard.FloatParser.floatParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.joml.Vector3f
import sendPrefixed
import util.error
import util.simpleSuggestion
import util.toXYZString

class PropCommands {

    init {
        val cm = ShulkerboxPaper.instance.commandManager
        val propCommandBase = cm.commandBuilder("prop")

        cm.command(propCommandBase.literal("create")
            .handler { ctx ->
                val player = ctx.sender() as Player
                val prop = player.world.spawnEntity(player.location.apply { pitch = 0f; yaw = 0f }.add(0.0, 0.5, 0.0), EntityType.ITEM_DISPLAY) as ItemDisplay
                prop.persistentDataContainer.set(ShulkerboxPaper.shulkerboxPropEntityTag, PersistentDataType.BOOLEAN, true)
                prop.setItemStack(ItemStack(Material.GLASS))
                PropManager.select(player, SelectedProp(prop, player))
                player.sendPrefixed("<green>Created new prop!")
            }
        )

        cm.command(propCommandBase.literal("remove")
            .handler { ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }
                PropManager.unselect(player)
                prop.itemDisplay.remove()
                player.sendPrefixed("<red>Removed a prop!")
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
                PropManager.unselect(player)
                val newPropEntity = player.world.spawnEntity(prop.itemDisplay.location, EntityType.ITEM_DISPLAY) as ItemDisplay
                val nbt = CompoundTag()
                (prop.itemDisplay as CraftEntity).handle.save(nbt)
                (newPropEntity as CraftEntity).handle.load(nbt)
                PropManager.select(player, SelectedProp(newPropEntity, player))
                player.sendPrefixed("<yellow>Cloned a prop!")
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
                val entities = player.location.getNearbyEntities(1.5, 3.0, 1.5).toMutableList().sortedBy { it.location.distance(player.location) }
                entities.forEach {
                    if(it.type != EntityType.ITEM_DISPLAY) return@forEach
                    if(it.persistentDataContainer.get(ShulkerboxPaper.shulkerboxPropEntityTag, PersistentDataType.BOOLEAN) != true) return@forEach
                    val itemDisplay = it as ItemDisplay
                    if(PropManager.propSelections[player] != null && PropManager.propSelections[player]!!.itemDisplay == itemDisplay) return@forEach
                    PropManager.select(player, SelectedProp(itemDisplay, player))
                    player.sendPrefixed("<green>Selected a prop!")
                }
            }
        )

        cm.command(propCommandBase.literal("size")
            .required("x", floatParser(), getCurrentPropSizeSuggestion("x"))
            .required("y", floatParser(), getCurrentPropSizeSuggestion("y"))
            .required("z", floatParser(), getCurrentPropSizeSuggestion("z"))
            .handler {ctx ->
                val player = ctx.sender() as Player
                val prop = PropManager.propSelections[player]
                val x = ctx.get<Float>("x")
                val y = ctx.get<Float>("y")
                val z = ctx.get<Float>("z")
                if(prop == null) {
                    error(player, "You do not have any prop selected")
                    return@handler
                }

                val current = prop.itemDisplay.transformation
                prop.itemDisplay.transformation = Transformation(current.translation, current.leftRotation, Vector3f(x, y, z), current.rightRotation)
                player.sendPrefixed("Set scale transform to <green>${prop.itemDisplay.transformation.scale}<yellow>!")
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
                prop.itemDisplay.setItemStack(item)
                player.sendPrefixed("Set the item of the prop to <green>${item.type.name}<gray>!")
                player.playEditSound()
            })

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
                if(arg == "-keepRot") {
                    location.pitch = prop.itemDisplay.location.pitch
                    location.yaw = prop.itemDisplay.location.yaw
                }
                if(arg == "-noRot") {
                    location.pitch = 0f
                    location.yaw = 0f
                }

                if(arg == "-keepYaw") {
                    location.pitch = prop.itemDisplay.location.pitch
                }

                if(arg == "-keepPitch") {
                    location.yaw = prop.itemDisplay.location.yaw
                }

                prop.itemDisplay.teleport(location)
                player.sendPrefixed("Teleported prop to <green>${location.toXYZString()}<yellow>!")
                player.playEditSound()
            })

    }

    private fun getCurrentPropSizeSuggestion(face: String): BlockingSuggestionProvider.Strings<CommandSender> {
        return BlockingSuggestionProvider.Strings { commandContext, input ->
            val player = commandContext.sender() as Player
            val out = mutableListOf<String>()
            val itemDisplay = PropManager.propSelections[player]
            if(itemDisplay != null) {
                val scale = when(face) {
                    "x" -> itemDisplay.itemDisplay.transformation.scale.x
                    "z" -> itemDisplay.itemDisplay.transformation.scale.y
                    "y" -> itemDisplay.itemDisplay.transformation.scale.z
                    else -> 0f
                }
                out.add("$scale")
            }
            out
        }
    }
}