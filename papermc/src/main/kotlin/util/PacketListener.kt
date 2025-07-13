package util

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPickItemFromEntity
import fakes.FakeInteraction

class PacketListener: PacketListener {

    override fun onPacketReceive(event: PacketReceiveEvent) {
        if(event.packetType != PacketType.Play.Client.PICK_ITEM_FROM_ENTITY) return
        val packet = WrapperPlayClientPickItemFromEntity(event)

        FakeInteraction.handlers[packet.entityId]?.invoke(event.getPlayer())
    }

}