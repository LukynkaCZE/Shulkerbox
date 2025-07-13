import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.mattmx.ktgui.GuiManager
import config.ConfigManager
import fakes.FakeListener
import git.GitIntegration
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import map.MapManager
import map.commands.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import props.PropAxisTool
import props.PropCommands
import props.PropListener
import selection.SelectionCommands
import selection.SelectionListener
import tools.SuperBarrierTool
import util.PacketListener
import youkai.YoukaiIntegration

class ShulkerboxPaper: JavaPlugin(), Listener {

    lateinit var commandManager: LegacyPaperCommandManager<CommandSender>
    companion object {
        lateinit var instance: ShulkerboxPaper
        var isBuildServer: Boolean = true
        var youkaiIntegration: Boolean = false
        var gitIntegration: Boolean = false
        lateinit var sidebarLibrary: ScoreboardLibrary
    }

    override fun onLoad() {
        super.onLoad()
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().eventManager.registerListener(PacketListener(), PacketListenerPriority.HIGHEST)
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
        FakeListener()

        MapCommand()
        BoundCommands()
        PointCommands()
        PropCommands()
        SelectionCommands()
        GamemodeCommands()
        ToolsCommand()

        AnnotationCommands()

        GuiManager.init(this)

        MapManager
        ResourcepackManager
        SuperBarrierTool
        PropAxisTool

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
            YoukaiIntegration.load(ConfigManager.currentConfig.youkai.youkaiSyncUrl)
            YoukaiIntegration.registerCommand()
        }
        if(gitIntegration) {
            GitIntegration.load()
        }

        @EventHandler
        fun motdEvent(event: ServerListPingEvent) {
            if(!ConfigManager.currentConfig.general.motd) return

            event.motd(ConfigManager.currentConfig.general.customMotd.toMiniMessage())
        }

        Bukkit.getPluginManager().registerEvents(this, this)

        PacketEvents.getAPI().init()
    }

    override fun onDisable() {
        if(youkaiIntegration) YoukaiIntegration.saveCache()
    }
}
