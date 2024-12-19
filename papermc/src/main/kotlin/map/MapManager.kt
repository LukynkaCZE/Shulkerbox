@file:Suppress("MemberVisibilityCanBePrivate")

package map

import CURRENT_SHULKERBOX_VERSION
import ShulkerboxMap
import ShulkerboxPaper
import ShulkerboxVector
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import config.ConfigManager
import files.MapFileReader
import files.MapFileWriter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector
import props.PropManager
import sendPrefixed
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.logging.Level

object MapManager : Listener {
    val maps = mutableMapOf<String, ShulkerboxMap>()
    val mapSessions = mutableMapOf<String, ActiveMapSession>()
    val mapSelections = mutableMapOf<Player, ActiveMapSession>()

    // to store what last map player had selected when they logged out
    val selectionCache = mutableMapOf<UUID, String>()

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val map = mapSelections[event.player]
        if (map != null) {
            selectionCache[event.player.uniqueId] = map.map.id
            unselect(event.player, true)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!ConfigManager.currentConfig.general.autoReselectMapAfterJoining) return
        val cachedMap = selectionCache[event.player.uniqueId] ?: return
        val map = maps[cachedMap] ?: return
        select(event.player, map, true)
    }

    private fun getOrCreateMapSession(map: ShulkerboxMap): ActiveMapSession {
        val existingSession = mapSessions[map.id]
        if (existingSession != null) return existingSession

        val newSession = ActiveMapSession(map)
        mapSessions[map.id] = newSession
        return newSession
    }

    fun select(player: Player, map: ShulkerboxMap, wasAutomatic: Boolean = false) {
        if (hasMapSelected(player)) unselect(player, true)

        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1.5f)
        val activeSession = getOrCreateMapSession(map)

        mapSelections[player] = activeSession
        activeSession.addViewer(player)

        val message = if (wasAutomatic) "Automatically selected your selected map (<yellow>${map.name}<gray>)" else "<gray>Selected map <yellow>${map.name}<gray>!"
        player.sendPrefixed(message)
    }

    fun unselect(player: Player, silent: Boolean = false) {
        val activeMap = mapSelections[player]!!

        if (PropManager.propSelections[player] != null) PropManager.unselect(player, false)

        activeMap.removeViewer(player)
        mapSelections.remove(player)

        if (activeMap.viewers.isEmpty()) {
            activeMap.dispose()
            mapSessions.remove(activeMap.map.id)
        }

        if (!silent) player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.5f)
        player.sendPrefixed("<gray>Unselected map <red>${activeMap.map.name}<gray>!")
    }

    fun hasMapSelected(player: Player): Boolean {
        return mapSelections.containsKey(player)
    }

    fun selectedShulkerboxMap(player: Player): ShulkerboxMap? {
        return mapSelections[player]?.map
    }

    private fun fromJson(input: String): ShulkerboxMap {
        val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
        val map = json.decodeFromString<ShulkerboxMap>(input)
        return map
    }

    private fun generateRegistryFileJson(): String {
        val entries = mutableListOf<ShulkerboxBuildServerRegistryEntry>()
        maps.forEach { map ->
            entries.add(ShulkerboxBuildServerRegistryEntry(map.key, map.value.origin!!))
        }
        val registry = ShulkerboxBuildServerRegistry(entries)
        val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
        val out = json.encodeToString<ShulkerboxBuildServerRegistry>(registry)
        return out
    }

    fun getWorldEditClipboardAndFEC(origin: Location, size: Vector): Pair<BlockArrayClipboard, ForwardExtentCopy> {

        val pos1 = BlockVector3.at(origin.x, origin.y, origin.z) // the map origin
        val origin2 = origin.clone().add(size)
        val pos2 = BlockVector3.at(origin2.x, origin2.y, origin2.z)
        val region = CuboidRegion(pos1, pos2)
        region.world = BukkitAdapter.asBukkitWorld(BukkitWorld(origin.world))

        val clipboard = BlockArrayClipboard(region)

        val forwardExtentCopy = ForwardExtentCopy(
            region.world, region, clipboard, clipboard.minimumPoint
        )
        forwardExtentCopy.isCopyingEntities = false
        return clipboard to forwardExtentCopy
    }

    fun getWorldEditClipboardAndFEC(map: ShulkerboxMap): Pair<Clipboard, ForwardExtentCopy> {
        return getWorldEditClipboardAndFEC(map.origin!!.toBukkitLocation(), map.size.toBukkitVector())
    }

    fun save(map: ShulkerboxMap) {
        val folder = File("plugins/Shulkerbox/temp/${map.id}/")
        folder.mkdirs()
        val file = File("plugins/Shulkerbox/temp/${map.id}/map.json")
        file.delete()
        file.createNewFile()
        file.writeText(map.toJson())
        map.version = CURRENT_SHULKERBOX_VERSION
        val minPoint = getWorldEditClipboardAndFEC(map).first.minimumPoint
        map.schematicToOriginOffset = ShulkerboxVector(minPoint.x, minPoint.y, minPoint.z).offsetTo(map.origin!!.toBukkitLocation().toVector().toShulkerboxVector())
        if (ShulkerboxPaper.isBuildServer) {
            val registryFolder = File("plugins/Shulkerbox/")
            registryFolder.mkdirs()
            val registryFile = File("plugins/Shulkerbox/build_server_registry.json")
            registryFile.delete()
            registryFile.createNewFile()
            registryFile.writeText(generateRegistryFileJson())
        }

        val forwardExtentCopy = getWorldEditClipboardAndFEC(map)
        Operations.complete(forwardExtentCopy.second)

        val schematicFile = File("plugins/Shulkerbox/temp/${map.id}/map.schem")
        BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(FileOutputStream(schematicFile)).use { writer ->
            writer.write(forwardExtentCopy.first)
        }

        MapFileWriter.writeMap(map)
    }

    @Suppress("UnstableApiUsage")
    fun loadMapsFromBuildServerRegistry() {
        val registryFile = File("plugins/Shulkerbox/build_server_registry.json")
        if (!registryFile.exists()) {
            File("plugins/Shulkerbox").mkdirs()
            registryFile.createNewFile()
            registryFile.writeText(generateRegistryFileJson())
        }

        val registry = Json.decodeFromString<ShulkerboxBuildServerRegistry>(registryFile.readText())
        registry.entries.forEach { entry ->
            val file = File("plugins/Shulkerbox/maps/${entry.mapId}.shulker")
            if (!file.exists()) {
                println("Error while loading ${entry.mapId}: Map file is not present in maps folder!")
                return@forEach
            }
            val mapJson = MapFileReader.load(file)
//            val map = fromJson(File("plugins/Shulkerbox/temp/${entry.mapId}/map.json").readText())
            val map = fromJson(mapJson.json)

            map.props.forEach propFixLoop@{ id, prop ->
                val propId = prop.youkaiModelId ?: return@propFixLoop
                prop.youkaiModelId = propId
            }

            map.origin = entry.location
            if (map.origin!!.toBukkitLocation().world == null) {
                throw Exception("uhh this should not be null: ${map.id} (${entry.location.world} | ${Bukkit.getWorlds().map { it.name }})")
            }
            maps[map.id] = map
            Bukkit.getLogger().log(Level.INFO, "Loaded map ${map.id}!")
        }
    }
}