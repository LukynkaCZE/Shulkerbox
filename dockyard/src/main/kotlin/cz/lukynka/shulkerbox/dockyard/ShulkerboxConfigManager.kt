@file:Suppress("UnstableApiUsage")

package cz.lukynka.shulkerbox.dockyard

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

object ShulkerboxConfigManager {

    var currentConfig: Config = Config()
    val toml = Toml(inputConfig = TomlInputConfig(ignoreUnknownNames = true, allowNullValues = false))

    fun load() {
        val file = File("./shulkerbox/config.toml")
        // create default config if it doesn't exist
        if(!file.exists()) {
            File("./shulkerbox/").mkdirs()
            file.createNewFile()
            file.writeText(toml.encodeToString<Config>(Config()))
        }

        currentConfig = toml.decodeFromString<Config>(file.readText())
        if(currentConfig.git.gitIntegrationEnabled) GitIntegration
        log("Loaded shulkerbox config!", LogType.SUCCESS)
    }

    fun save() {
        val file = File("./shulkerbox/config.toml")
        file.writeText(toml.encodeToString<Config>(currentConfig))
    }

    @Serializable
    data class Config(
        @SerialName("Youkai")
        val youkai: YoukaiIntegration = YoukaiIntegration(),
        @SerialName("Git")
        val git: GitIntegration = GitIntegration()
    )

    @Serializable
    data class YoukaiIntegration(
        val youkaiIntegrationEnabled: Boolean = false,
    )

    @Serializable
    data class GitIntegration(
        val gitIntegrationEnabled: Boolean = false,
        val gitUrl: String = "",
        val gitUser: String = "",
        val gitPassword: String = "",
    )
}