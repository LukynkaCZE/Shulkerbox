package fakes

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.util.Brightness
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.Level
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation

class FakeItemDisplay(override var location: Location) : FakeEntity {
    override val viewerPlayers: MutableSet<Player> = mutableSetOf()
    override val entity: Display.ItemDisplay = Display.ItemDisplay(EntityType.ITEM_DISPLAY, location.world.getMinecraftLevel())

    init {
        entity.setLocation(location)

        entity.transformationInterpolationDelay = 0
        entity.transformationInterpolationDuration = 5
    }

    fun setGlowColor(color: Color) {
        entity.glowColorOverride = color.asRGB()
        sendMetadata()
    }

    fun setBrightness(brightness: org.bukkit.entity.Display.Brightness) {
        entity.brightnessOverride = Brightness(brightness.blockLight, brightness.skyLight)
        sendMetadata()
    }

    fun setTransformation(transform: Transformation) {
        entity.setTransformation(transform.getMojangTransformation())
        sendMetadata()
    }

    fun getTransformation(): Transformation {
        return entity.renderState()!!.transformation.get(1f).toBukkit()
    }

    fun setRotation(newYaw: Float, newPitch: Float) {
        teleport(location.clone().apply { yaw = newYaw; pitch = newPitch })
    }

    fun setTransform(transform: ItemDisplayTransform) {
        entity.itemTransform = transform.getVanilla()
        sendMetadata()
    }

    fun setItem(itemStack: ItemStack) {
        entity.itemStack = itemStack.vanilla
        sendMetadata()
    }

    override fun addViewer(player: Player) {
        viewerPlayers.add(player)
        player.send(spawnEntityPacket(entity))
        sendMetadata(player)
        teleport(location)
    }

    override fun removeViewer(player: Player) {
        player.send(despawnEntityPacket(entity))
        viewerPlayers.remove(player)
    }

    override fun despawn() {
        viewerPlayers.forEach(::removeViewer)
        viewerPlayers.clear()
    }

    override fun teleport(location: Location) {
        this.location = location
        entity.setLocation(location)
        viewerPlayers.forEach { it.send(ClientboundTeleportEntityPacket(entity)) }
    }

    fun sendMetadata(player: Player? = null) {
        val players: List<Player> = if(player == null) viewerPlayers.toList() else listOf(player)
        val entityMetadataPacket = ClientboundSetEntityDataPacket(this.entity.id, this.entity.entityData.packAll()!!)
        players.forEach { it.send(entityMetadataPacket) }
    }

    override fun setGlowing(boolean: Boolean) {
        this.entity.setGlowingTag(boolean)
        sendMetadata()
    }
}

fun Entity.setLocation(location: Location) {
    this.moveTo(location.x, location.y, location.z, location.yaw, location.pitch)
    this.yHeadRot = location.yaw
    this.setLevel(location.world.getMinecraftLevel())
}

fun World.getMinecraftLevel(): Level {
    return (this as CraftWorld).handle
}

fun spawnEntityPacket(entity: Entity): ClientboundAddEntityPacket {
    return ClientboundAddEntityPacket(
        entity,
        entity.id,
        entity.blockPosition()
    )
}

fun despawnEntityPacket(entity: Entity): ClientboundRemoveEntitiesPacket {
    return ClientboundRemoveEntitiesPacket(entity.id)
}

fun Player.send(vararg packets: Packet<*>) {
    val craftPlayer = (this as CraftPlayer)
    packets.forEach { craftPlayer.handle.connection.send(it) }
}

val ItemStack.vanilla: net.minecraft.world.item.ItemStack get() = CraftItemStack.asCraftCopy(this).handle

fun ItemDisplayTransform.getVanilla(): ItemDisplayContext {
    val vanillaTransform = when(this) {
        ItemDisplayTransform.NONE -> ItemDisplayContext.NONE
        ItemDisplayTransform.THIRDPERSON_LEFTHAND -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND
        ItemDisplayTransform.THIRDPERSON_RIGHTHAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
        ItemDisplayTransform.FIRSTPERSON_LEFTHAND -> ItemDisplayContext.FIRST_PERSON_LEFT_HAND
        ItemDisplayTransform.FIRSTPERSON_RIGHTHAND -> ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
        ItemDisplayTransform.HEAD -> ItemDisplayContext.HEAD
        ItemDisplayTransform.GUI -> ItemDisplayContext.GUI
        ItemDisplayTransform.GROUND -> ItemDisplayContext.GROUND
        ItemDisplayTransform.FIXED -> ItemDisplayContext.FIXED
    }
    return vanillaTransform
}

fun Transformation.getMojangTransformation(): com.mojang.math.Transformation {
    return com.mojang.math.Transformation(this.translation, this.leftRotation, this.scale, this.rightRotation)
}

fun com.mojang.math.Transformation.toBukkit(): Transformation {
    return Transformation(
        this.translation,
        this.leftRotation,
        this.scale,
        this.rightRotation
    )
}