import essentials.commands.DisplayTest
import essentials.commands.GamemodeCommands
import map.MapManager
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import map.commands.MapCommand
import map.commands.BoundCommands
import map.commands.PointCommands
import props.PropCommands
import props.PropListener
import selection.SelectionCommands
import selection.SelectionListener
import youkai.YoukaiIntegration

class ShulkerboxPaper: JavaPlugin() {

    lateinit var commandManager: LegacyPaperCommandManager<CommandSender>
    companion object {
        lateinit var instance: ShulkerboxPaper
        lateinit var namespacedKey: NamespacedKey
        lateinit var shulkerboxBoundingBoxEntityTag: NamespacedKey
        lateinit var shulkerboxPropEntityTag: NamespacedKey
        var isBuildServer: Boolean = true
        var youkaiSupport: Boolean = true
    }

    override fun onEnable() {

        instance = this
        namespacedKey = NamespacedKey(instance, "shulkerbox")
        shulkerboxBoundingBoxEntityTag = NamespacedKey(instance, "is_bounding_box_entity")
        shulkerboxBoundingBoxEntityTag = NamespacedKey(instance, "is_bounding_box_entity")
        shulkerboxPropEntityTag = NamespacedKey(instance, "prop_id")

        isBuildServer = true
        if(youkaiSupport) {
            YoukaiIntegration.start()
        }

        this.commandManager = LegacyPaperCommandManager(
            this,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.identity(),
        )

        SelectionListener()
        PropListener()

        MapCommand()
        BoundCommands()
        PointCommands()
        PropCommands()
        SelectionCommands()
        GamemodeCommands()

        DisplayTest()

        Bukkit.getScheduler().scheduleSyncDelayedTask(this) {
            if (isBuildServer) {
                try {
                    MapManager.loadMapsFromBuildServerRegistry()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        if(youkaiSupport) {
            YoukaiIntegration.loadCache()
        }
    }

    override fun onDisable() {
        Bukkit.broadcastMessage("bai :3")
        if(youkaiSupport) {
            YoukaiIntegration.saveCache()
        }
    }
}

fun clearEntitiesTemp() {
    Bukkit.getWorlds().forEach { world ->
        world.entities.forEach { entity ->
            if(entity.persistentDataContainer.get(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN) == true) {
                entity.remove()
            }
        }
    }
}