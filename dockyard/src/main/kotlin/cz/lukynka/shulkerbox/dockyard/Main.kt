package cz.lukynka.shulkerbox.dockyard

import io.github.dockyardmc.DockyardServer
import io.github.dockyardmc.commands.Commands
import io.github.dockyardmc.events.Events
import io.github.dockyardmc.events.PlayerJoinEvent
import io.github.dockyardmc.utils.Quaternion
import io.github.dockyardmc.utils.vectors.Vector3f
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
    }

    Commands.add("/load") {
        execute {
            val player = it.getPlayerOrThrow()
            val shulkerboxMap = MapFileReader.read(File("dockyard/maps/emberseeker_hub.shulker"))
            val map = DockyardMap.fromShulkerboxMap(shulkerboxMap, player.location)
            map.placeSchematic()
            map.spawnProps()
        }
    }

    server.start()
}

fun quaternionToVector3f(quaternion: Quaternion): Vector3f {
    val x = 2 * (quaternion.w * quaternion.x + quaternion.y * quaternion.z)
    val y = 2 * (quaternion.w * quaternion.y - quaternion.x * quaternion.z)
    val z = 1 - 2 * (quaternion.x * quaternion.x + quaternion.y * quaternion.y)
    return Vector3f(x, y, z)
}