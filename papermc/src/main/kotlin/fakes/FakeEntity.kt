package fakes

import org.bukkit.Location
import net.minecraft.world.entity.Entity
import org.bukkit.entity.Player

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

}