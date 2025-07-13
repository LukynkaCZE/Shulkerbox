package fakes

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import org.bukkit.Location
import net.minecraft.world.entity.Entity
import org.bukkit.entity.Player
import util.sendPacket

interface FakeEntity {

    var location: Location
    val viewerPlayers: MutableSet<Player>
    val entity: Entity

    fun getViewers(): List<Player> = viewerPlayers.toList()
    fun addViewer(player: Player)
    fun removeViewer(player: Player)

    fun despawn()

    fun teleport(location: Location)
    fun setGlowing(boolean: Boolean)

    fun sendMetadata(player: Player? = null) {
        val players: List<Player> = if(player == null) viewerPlayers.toList() else listOf(player)
        val entityMetadataPacket = ClientboundSetEntityDataPacket(this.entity.id, this.entity.entityData.packAll()!!)
        players.forEach { it.sendPacket(entityMetadataPacket) }
    }


}