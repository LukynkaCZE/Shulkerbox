import essentials.commands.GamemodeCommands
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
import selection.SelectionCommands
import selection.SelectionListener

class ShulkerboxPaper: JavaPlugin() {

    lateinit var commandManager: LegacyPaperCommandManager<CommandSender>
    companion object {
        lateinit var instance: ShulkerboxPaper
        lateinit var namespacedKey: NamespacedKey
        lateinit var shulkerboxBoundingBoxEntityTag: NamespacedKey
        lateinit var shulkerboxPointInteractionTag: NamespacedKey
    }

    override fun onEnable() {

        instance = this
        namespacedKey = NamespacedKey(instance, "shulkerbox")
        shulkerboxBoundingBoxEntityTag = NamespacedKey(instance, "is_bounding_box_entity")
        shulkerboxPointInteractionTag = NamespacedKey(instance, "point_interaction_owner")

        this.commandManager = LegacyPaperCommandManager(
            this,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.identity(),
        )

        GamemodeCommands()
        SelectionListener
        SelectionCommands()


        MapCommand()
        BoundCommands()
        PointCommands()
    }

    override fun onDisable() {
        Bukkit.broadcastMessage("bai :3")
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