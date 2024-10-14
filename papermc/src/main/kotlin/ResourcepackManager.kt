import config.ConfigManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.incendo.cloud.parser.standard.StringParser.stringParser

object ResourcepackManager: Listener {

    fun getFromConfig(): String? {
        return ConfigManager.currentConfig.general.packUrl
    }

    fun set(url: String) {
        ConfigManager.currentConfig.general.packUrl = url
        ConfigManager.save()
        resend()
    }

    fun resend() {
        Bukkit.getOnlinePlayers().forEach { it.setResourcePack(getFromConfig()!!) }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if(getFromConfig() == null) return
        player.setResourcePack(getFromConfig()!!)
    }

    init {
        val instance = ShulkerboxPaper.instance
        val command = instance.commandManager.commandBuilder("packurl")
            .permission("shulkerbox.admin")
            .required("url", stringParser())
            .handler {
                set(it.get<String>("url"))
            }

        instance.commandManager.command(command)
        Bukkit.getPluginManager().registerEvents(this, instance)
    }
}