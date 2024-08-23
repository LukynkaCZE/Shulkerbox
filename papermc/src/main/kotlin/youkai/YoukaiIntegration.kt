package youkai

import ShulkerboxPaper
import cz.lukynka.lkws.LightweightWebServer
import cz.lukynka.lkws.responses.Response
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import map.MapManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import selection.SelectionManager.prefix
import toMiniMessage
import util.runLater
import java.io.File

object YoukaiIntegration {

    val models = mutableMapOf<String, YoukaiServerModel>()

    private val webServer = LightweightWebServer(6969)
    private val youkaiToken = "qDSIerBkcfXCudkjDd296esWfAEBFB7sZtyKaBaHvqta2RfTP5b3S5GuddbRbofC"

    var cache: YoukaiSync? = null

    private fun youkaiAuth(response: Response): Boolean {
        return response.requestHeaders["Authorization"] == youkaiToken
    }

    fun saveCache() {
        if(cache == null) return
        val path = "Shulkerbox/youkai.json"
        val outFile = File(path)
        outFile.mkdirs()
        outFile.delete()
        outFile.createNewFile()

        val json = Json { ignoreUnknownKeys = true }
        val content = json.encodeToString<YoukaiSync>(cache!!)
        outFile.writeText(content)
    }

    fun loadCache() {
        val path = "Shulkerbox/youkai.json"
        val outFile = File(path)
        if(!outFile.exists()) return
        val json = Json { ignoreUnknownKeys = true }
        val content = json.decodeFromString<YoukaiSync>(outFile.readText())
        cache = content
        content.models.forEach { model ->
            models[model.modelId] = model
        }
    }

    fun getModel(id: String): ItemStack {
        val model = models[id]
        if(model == null) {
            val item = ItemStack(Material.BARRIER)
            item.editMeta { it.displayName("<red><bold>No Youkai Model </bold>:: <yellow>$id".toMiniMessage()) }
            return item
        }
        val item = ItemStack(Material.valueOf(model.baseMaterial.uppercase()))
        item.editMeta {
            it.setCustomModelData(model.customModelId)
        }
        return item
    }

    fun start() {

        webServer.get("/") {
            it.respond("Shulkerbox Server; Youkai Enabled")
        }

        webServer.post("/youkai-sync", ::youkaiAuth) {
            val json = Json { ignoreUnknownKeys = true }
            val sync = json.decodeFromString<YoukaiSync>(it.requestBody)
            it.respond("ok")
            cache = sync
            models.clear()
            saveCache()
            loadCache()
            Bukkit.broadcast("$prefix <gold>Received sync request from youkai.. Updating models!".toMiniMessage())
            runLater(0) {
                MapManager.mapSelections.values.forEach { map ->
                    map.dispose()
                    map.updateDrawables()
                    map.drawableProps.filter { prop -> prop.prop.youkaiModelId != null }.forEach { prop ->
                        Bukkit.broadcast("$prefix <gold>Updating prop <yellow>${prop.prop.uid}<gray> with youkai id <aqua>${prop.prop.youkaiModelId!!}..".toMiniMessage())
                    }
                }
            }
        }
    }
}

@Serializable
data class YoukaiSync(
    val models: List<YoukaiServerModel>
)

@Serializable
data class YoukaiServerModel(
    val modelId: String,
    var customModelId: Int,
    val baseMaterial: String
)