package fakes

import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.entity.Display.TextDisplay.DATA_BACKGROUND_COLOR_ID
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PositionMoveRotation
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Player
import toMiniMessage
import util.*

class FakeTextDisplay(override var location: Location) : FakeEntity {
    override val viewerPlayers: MutableSet<Player> = mutableSetOf()
    override val entity: TextDisplay = TextDisplay(EntityType.TEXT_DISPLAY, location.world.getMinecraftLevel())

    init {
        entity.setLocation(location)
        entity.viewRange = 999999f
    }

    fun setText(text: String) {
        setText(text.toMiniMessage())
    }

    fun setText(component: Component) {
        entity.text = component.toVanilla()
        sendMetadata()
    }

    fun setBillboard(billboard: Billboard) {
        entity.billboardConstraints = billboard.toVanilla()
        sendMetadata()
    }

    fun setBackground(background: Int) {
        entity.entityData.set(DATA_BACKGROUND_COLOR_ID, background)
        sendMetadata()
    }

    fun setShadow(shadow: Boolean) {
        val nbt = CompoundTag()
        entity.save(nbt)
        nbt.putBoolean("shadow", shadow)
        entity.load(nbt)
        sendMetadata()
    }

    fun setSeeThrough(seeThrough: Boolean) {
        val nbt = CompoundTag()
        entity.save(nbt)
        nbt.putBoolean("see_through", seeThrough)
        entity.load(nbt)
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
        viewerPlayers.forEach { it.send(ClientboundTeleportEntityPacket(entity.id, PositionMoveRotation.of(entity), setOf(), entity.onGround)) }
    }

    override fun setGlowing(boolean: Boolean) {
        this.entity.setGlowingTag(boolean)
        sendMetadata()
    }

    fun setGlowColor(color: Color) {
        entity.glowColorOverride = color.asRGB()
        sendMetadata()
    }

    private fun sendMetadata(player: Player? = null) {
        val players: List<Player> = if(player == null) viewerPlayers.toList() else listOf(player)
        val entityMetadataPacket = ClientboundSetEntityDataPacket(this.entity.id, this.entity.entityData.packAll()!!)
        players.forEach { it.send(entityMetadataPacket) }
    }
}

