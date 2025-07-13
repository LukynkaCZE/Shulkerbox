package cz.lukynka

import cz.lukynka.shulkerbox.minestom.ShulkerboxIntegration
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block

fun main() {
    println("Hello World!")

    val server = MinecraftServer.init()
    val commandManager = MinecraftServer.getCommandManager()
    commandManager.register(ShulkerboxIntegration())

    val instanceManager = MinecraftServer.getInstanceManager()
    val hub = instanceManager.createInstanceContainer()
    hub.setGenerator {
        it.modifier().fillHeight(0, 40, Block.STONE)
    }

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        event.spawningInstance = hub
        event.player.respawnPoint = Pos(0.5, 42.0, 0.5)
    }

    hub.setChunkSupplier(::LightingChunk)
    server.start("0.0.0.0", 25565)
}