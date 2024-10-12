package cz.lukynka.shulkerbox.dockyard

import cz.lukynka.shulkerbox.dockyard.conversion.toDockyardMap
import io.github.dockyardmc.DockyardServer
import io.github.dockyardmc.commands.Commands
import io.github.dockyardmc.events.Events
import io.github.dockyardmc.events.PlayerJoinEvent
import io.github.dockyardmc.player.GameMode
import io.github.dockyardmc.registry.PotionEffects
import java.io.File

fun main() {
    val server = DockyardServer {
        withIp("0.0.0.0")
        withPort(25565)
        useMojangAuth(true)
        withImplementations {
            dockyardCommands = true
        }
    }

    Events.on<PlayerJoinEvent> {
        it.player.permissions.add("dockyard.*")
        it.player.gameMode.value = GameMode.CREATIVE
        it.player.addPotionEffect(PotionEffects.NIGHT_VISION, 99999, 1)
    }

    var map: DockyardMap? = null

    Commands.add("/load") {
        execute {
            val player = it.getPlayerOrThrow()
            val shulkerboxMap = MapFileReader.read(File("dockyard/maps/emberseeker_hub.shulker"))
            map = shulkerboxMap.toDockyardMap(player.location.getBlockLocation())
            map!!.placeSchematic()
            map!!.spawnProps()
        }
    }

    Commands.add("/move") {
        execute {
            val player = it.getPlayerOrThrow()
            map!!.spawnedProps.forEach { prop ->
                prop.value.teleport(prop.key.location.clone().add(player.location))
            }
            val first = map!!.spawnedProps.keys.first()
            val off = first.location.clone().subtract(player.location)
            player.sendMessage("<pink>$off")
        }
    }

    server.start()
}