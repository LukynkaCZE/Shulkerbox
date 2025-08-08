package util

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Display.BillboardConstraints
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.Level
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.CraftBlockState
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation

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

fun Entity.getSpawnPacket(): ClientboundAddEntityPacket {
    return ClientboundAddEntityPacket(
        this,
        this.id,
        this.blockPosition()
    )
}

fun Entity.getDespawnPacket(): ClientboundRemoveEntitiesPacket {
    return ClientboundRemoveEntitiesPacket(this.id)
}

fun Player.sendPacket(vararg packets: Packet<*>) {
    val craftPlayer = (this as CraftPlayer)
    packets.forEach { craftPlayer.handle.connection.send(it) }
}

fun ItemStack.getVanilla(): net.minecraft.world.item.ItemStack = CraftItemStack.asCraftCopy(this).handle

fun BlockState.getVanilla(): net.minecraft.world.level.block.state.BlockState = (this as CraftBlockState).handle

fun ItemDisplayTransform.getVanilla(): ItemDisplayContext {
    val vanillaTransform = when (this) {
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

fun Entity.setLocation(location: Location) {
    this.teleportTo(location.world.getMinecraftLevel() as ServerLevel, location.x, location.y, location.z, setOf(), location.yaw, location.pitch, true)
    this.yHeadRot = location.yaw
    this.setLevel(location.world.getMinecraftLevel())
}

fun World.getMinecraftLevel(): Level {
    return (this as CraftWorld).handle
}

fun Component.toVanilla(): net.minecraft.network.chat.Component {
    val json: String = GsonComponentSerializer.gson().serialize(this)
    return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow()
}

fun Billboard.toVanilla(): BillboardConstraints {
    return when (this) {
        Billboard.FIXED -> BillboardConstraints.FIXED
        Billboard.VERTICAL -> BillboardConstraints.VERTICAL
        Billboard.HORIZONTAL -> BillboardConstraints.HORIZONTAL
        Billboard.CENTER -> BillboardConstraints.CENTER
    }
}

fun Boolean.toColoredString(): String {
    return if (this) "<green>true" else "<red>false"
}