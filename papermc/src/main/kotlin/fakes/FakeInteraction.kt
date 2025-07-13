package fakes

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Interaction
import net.minecraft.world.entity.PositionMoveRotation
import org.bukkit.Location
import org.bukkit.entity.Player
import util.*

class FakeInteraction(override var location: Location) : FakeEntity {

    companion object {
        val handlers: MutableMap<Int, (Player) -> Unit> = mutableMapOf()
    }

    override val viewerPlayers: MutableSet<Player> = mutableSetOf()
    override val entity: Interaction = Interaction(EntityType.INTERACTION, location.world.getMinecraftLevel())

    init {
        entity.setLocation(location)
        entity.height = 1f
        entity.width = 1f
    }

    fun addPickHandler(handler: (Player) -> Unit) {
        handlers[entity.id] = handler
    }

    override fun addViewer(player: Player) {
        viewerPlayers.add(player)
        player.sendPacket(entity.getSpawnPacket())
        sendMetadata(player)
        teleport(location)
    }

    override fun removeViewer(player: Player) {
        player.sendPacket(entity.getDespawnPacket())
        viewerPlayers.remove(player)
    }

    override fun despawn() {
        viewerPlayers.toList().forEach(::removeViewer)
        viewerPlayers.clear()
        handlers.remove(this.entity.id)
    }

    override fun teleport(location: Location) {
        this.location = location
        entity.setLocation(location)
        viewerPlayers.forEach { it.sendPacket(ClientboundTeleportEntityPacket(entity.id, PositionMoveRotation.of(entity), setOf(), entity.onGround)) }
    }

    override fun setGlowing(boolean: Boolean) {}
}