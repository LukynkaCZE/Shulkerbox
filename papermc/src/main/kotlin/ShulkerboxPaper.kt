import config.ConfigManager
import git.GitIntegration
import map.MapManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import map.commands.MapCommand
import map.commands.BoundCommands
import map.commands.PointCommands
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary
import props.PropCommands
import props.PropListener
import selection.SelectionCommands
import selection.SelectionListener
import youkai.YoukaiIntegration

class ShulkerboxPaper: JavaPlugin() {

    lateinit var commandManager: LegacyPaperCommandManager<CommandSender>
    companion object {
        lateinit var instance: ShulkerboxPaper
        var isBuildServer: Boolean = true
        var youkaiIntegration: Boolean = false
        var gitIntegration: Boolean = false
        lateinit var sidebarLibrary: ScoreboardLibrary
    }

    override fun onEnable() {

        instance = this
        sidebarLibrary = ScoreboardLibrary.loadScoreboardLibrary(instance);
        ConfigManager.load()

        isBuildServer = true // always true if loaded as plugin
        gitIntegration = ConfigManager.currentConfig.git.gitIntegrationEnabled

        val youkaiConfig = ConfigManager.currentConfig.youkai
        youkaiIntegration = youkaiConfig.youkaiIntegrationEnabled
        if(youkaiIntegration) {
            YoukaiIntegration.youkaiToken = youkaiConfig.youkaiAuthToken
            YoukaiIntegration.start(youkaiConfig.youkaiWebServerPort)
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

        MapManager

        Bukkit.getScheduler().scheduleSyncDelayedTask(this) {
            if (isBuildServer) {
                try {
                    MapManager.loadMapsFromBuildServerRegistry()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        if(youkaiIntegration) {
            YoukaiIntegration.loadCache()
        }
        if(gitIntegration) {
            GitIntegration.load()
        }
    }

    override fun onDisable() {
        if(youkaiIntegration) YoukaiIntegration.saveCache()
    }
}
