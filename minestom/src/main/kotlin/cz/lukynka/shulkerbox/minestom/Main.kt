//package cz.lukynka.shulkerbox.dockyard
//
//import java.io.File
//import kotlin.time.Duration
//
//fun main() {
//    val server = DockyardServer {
//        withIp("0.0.0.0")
//        withPort(25565)
//        useMojangAuth(true)
//        withImplementations {
//            defaultCommands = true
//        }
//    }
//
//    YoukaiPack.load("https://cdn.lukynka.cloud/ember-seeker-pack.json")
//    YoukaiPack.update()
//
//    Events.on<PlayerJoinEvent> {
//        it.player.permissions.add("dockyard.*")
//        it.player.gameMode.value = GameMode.CREATIVE
//        it.player.addPotionEffect(PotionEffects.NIGHT_VISION, Duration.INFINITE, 1)
//    }
//
//    Commands.add("/load") {
//        execute {
//            val player = it.getPlayerOrThrow()
//            val map = MapFileReader.read(File("shulkerbox/maps/emberseeker_hub.shulker"))
//                .toDockyardMap(player.location.getBlockLocation())
//            val spawn = map.getPoint("spawn")
//
//            map.placeSchematicAsync().thenAccept {
//                DockyardServer.broadcastMessage("<lime>Map loaded!!")
//                player.teleport(spawn.location)
//                map.spawnProps()
//            }
//        }
//    }
//    server.start()
//    ShulkerboxIntegration.load()
//}
//
//fun <T> MutableList<T>.consumeRandom(): T {
//    val random = this.random()
//    this.remove(random)
//    return random
//}