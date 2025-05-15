@file:Suppress("UnstableApiUsage")

package config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.bukkit.Bukkit
import selection.SelectionManager
import java.io.File
import java.util.logging.Level

object ConfigManager {

    var currentConfig: Config = Config()
    val toml = Toml(inputConfig = TomlInputConfig(ignoreUnknownNames = true, allowNullValues = false))

    fun load() {
        val file = File("plugins/Shulkerbox/config.toml")
        // create default config if it doesn't exist
        if(!file.exists()) {
            File("plugins/Shulkerbox/").mkdirs()
            file.createNewFile()
            file.writeText(toml.encodeToString<Config>(Config()))
        }

        currentConfig = toml.decodeFromString<Config>(file.readText())
        Bukkit.getLogger().log(Level.CONFIG, "Loaded Shulkerbox config!")
    }

    fun save() {
        val file = File("plugins/Shulkerbox/config.toml")
        file.writeText(toml.encodeToString<Config>(currentConfig))
    }

    @Serializable
    data class Config(
        @SerialName("General")
        val general: General = General(),
        @SerialName("Youkai")
        val youkai: YoukaiIntegration = YoukaiIntegration(),
        @SerialName("Git")
        val git: GitIntegration = GitIntegration()
    )

    @Serializable
    data class General(
        val sidebar: Boolean = true,
        val autoReselectMapAfterJoining: Boolean = true,
        var packUrl: String? = null,
        var motd: Boolean = true,
        var customMotd: String = "${SelectionManager.prefix} <gray>Shulkerbox Building Server"
    )

    @Serializable
    data class YoukaiIntegration(
        val youkaiIntegrationEnabled: Boolean = false,
        val youkaiAuthToken: String = "",
        val youkaiWebServerPort: Int = 6969,
        val youkaiSyncUrl: String = ""
    )

    @Serializable
    data class GitIntegration(
        val gitIntegrationEnabled: Boolean = false,
        val gitUrl: String = "",
        val gitUser: String = "",
        val gitPassword: String = "",
        val authorIsMinecraftUsername: Boolean = true
    )
}