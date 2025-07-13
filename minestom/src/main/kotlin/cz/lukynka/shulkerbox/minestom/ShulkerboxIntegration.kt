package cz.lukynka.shulkerbox.minestom

import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player
import java.io.File

class ShulkerboxIntegration : Command("place") {

    init {
        defaultExecutor = CommandExecutor { sender, context ->
            val player = sender as Player
            val instance = player.instance
            val location = player.position

            val map = MapFileReader.read(File("./shulkerbox/maps/emberseeker_hub.shulker")).toMinestomMap(location, instance)
            map.placeSchematic()
        }
    }
}