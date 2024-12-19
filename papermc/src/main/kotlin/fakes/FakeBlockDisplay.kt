package fakes

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.util.Brightness
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.BlockState
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import util.*

class FakeBlockDisplay(override var location: Location) : FakeEntity {
    override val viewerPlayers: MutableSet<Player> = mutableSetOf()
    override val entity: Display.BlockDisplay = Display.BlockDisplay(EntityType.BLOCK_DISPLAY, location.world.getMinecraftLevel())

    init {
        entity.setLocation(location)

        entity.transformationInterpolationDelay = 0
        entity.transformationInterpolationDuration = 2
        (entity.bukkitEntity as BlockDisplay).interpolationDuration = 2
        (entity.bukkitEntity as BlockDisplay).teleportDuration = 2
        entity.viewRange = 999999f

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
        return (entity.bukkitEntity as BlockDisplay).transformation
    }

    fun setRotation(newYaw: Float, newPitch: Float) {
        teleport(location.clone().apply { yaw = newYaw; pitch = newPitch })
    }

    fun setBlock(block: BlockState) {
        entity.blockState = block.getVanilla()
        sendMetadata()
    }

    override fun addViewer(player: Player) {
        viewerPlayers.add(player)
        player.send(entity.getSpawnPacket())
        sendMetadata(player)
        teleport(location)
    }

    override fun removeViewer(player: Player) {
        player.send(entity.getDespawnPacket())
        viewerPlayers.remove(player)
    }

    override fun despawn() {
        viewerPlayers.toList().forEach(::removeViewer)
        viewerPlayers.clear()
    }

    override fun teleport(location: Location) {
        this.location = location
        entity.setLocation(location)
        viewerPlayers.forEach { it.send(ClientboundTeleportEntityPacket(entity)) }
    }

    private fun sendMetadata(player: Player? = null) {
        val players: List<Player> = if(player == null) viewerPlayers.toList() else listOf(player)
        val entityMetadataPacket = ClientboundSetEntityDataPacket(this.entity.id, this.entity.entityData.packAll()!!)
        players.forEach { it.send(entityMetadataPacket) }
    }

    override fun setGlowing(boolean: Boolean) {
        this.entity.setGlowingTag(boolean)
        sendMetadata()
    }
}