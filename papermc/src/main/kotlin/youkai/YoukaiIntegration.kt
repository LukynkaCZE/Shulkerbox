package youkai

import ResourcepackManager
import ShulkerboxPaper
import cz.lukynka.lkws.LightweightWebServer
import cz.lukynka.lkws.responses.Response
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import map.MapManager
import map.toPropItemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.components.CraftCustomModelDataComponent
import org.bukkit.inventory.ItemStack
import selection.SelectionManager.prefix
import toMiniMessage
import util.runLater
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

object YoukaiIntegration {

    val models = mutableMapOf<String, YoukaiServerModel>()

    lateinit var webServer: LightweightWebServer
    var youkaiToken = ""
    lateinit var syncUrl: String

    var cache: YoukaiSync? = null

    fun registerCommand() {
        val cm = ShulkerboxPaper.instance.commandManager
        val builder = cm.commandBuilder("updateyoukai")
            .permission("shulkerbox.cmd")
            .handler { ctx ->
                update()
            }
            .build()

        cm.command(builder)
    }

    fun load(syncUrl: String) {
        this.syncUrl = syncUrl
        update()
    }

    private fun youkaiAuth(response: Response): Boolean {
        return response.requestHeaders["Authorization"] == youkaiToken
    }

    fun update() {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI(syncUrl))
            .build()

        client.sendAsync(request, BodyHandlers.ofString())
            .thenAccept { response ->
                val body = response.body()
                val sync = Json.decodeFromString<YoukaiSync>(body)
                models.clear()
                sync.models.forEach { model ->
                    println("Fetched ${model.modelId}!")
                    models[model.modelId] = model
                }
                println("Fetched ${sync.models.size} youkai models!")
                saveCache()

                runLater(0) {
                    MapManager.mapSelections.values.forEach { map ->
                        map.dispose()
                        map.updateDrawables()
                        map.drawableProps.filter { prop -> prop.prop.youkaiModelId != null }.forEach { prop ->
                            Bukkit.broadcast("$prefix <gold>Updating prop <yellow>${prop.prop.uid}<gray> with youkai id <aqua>${prop.prop.youkaiModelId!!}..".toMiniMessage())
                        }
                    }
                    MapManager.maps.forEach { map ->
                        map.value.props.filter { prop -> prop.value.youkaiModelId != null }.forEach { prop ->
                            prop.value.itemStack = getModel(prop.value.youkaiModelId!!).toPropItemStack()
                        }
                    }
                    ResourcepackManager.resend()
                }
            }
            .exceptionally {
                it.printStackTrace()
                null
            }
    }

    fun saveCache() {
        if (cache == null) return
        val path = "plugins/Shulkerbox/youkai.json"
        val outFile = File(path)
        outFile.mkdirs()
        outFile.delete()
        outFile.createNewFile()

        val json = Json { ignoreUnknownKeys = true }
        val content = json.encodeToString<YoukaiSync>(cache!!)
        outFile.writeText(content)
    }

    fun loadCache() {
        val path = "plugins/Shulkerbox/youkai.json"
        val outFile = File(path)
        if (!outFile.exists()) return
        val json = Json { ignoreUnknownKeys = true }
        val content = json.decodeFromString<YoukaiSync>(outFile.readText())
        cache = content
        content.models.forEach { model ->
            models[model.modelId] = model
        }
    }

    fun getModel(id: String): ItemStack {
        val model = models[id]
        if (model == null) {
            val item = ItemStack(Material.BARRIER)
            item.editMeta { it.displayName("<red><bold>No Youkai Model </bold>:: <yellow>$id".toMiniMessage()) }
            Bukkit.broadcast("$prefix <red><bold>No Youkai Model </bold>:: <yellow>$id".toMiniMessage())
            return item
        }
        val item = ItemStack(Material.valueOf(model.baseMaterial.uppercase()))
        item.editMeta {
            val component = CraftCustomModelDataComponent(mapOf())
            component.floats = listOf(model.customModelId.toFloat())
            it.setCustomModelDataComponent(component)
        }
        return item
    }

    fun start(port: Int) {

        webServer = LightweightWebServer(port)

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
                MapManager.maps.forEach { map ->
                    map.value.props.filter { prop -> prop.value.youkaiModelId != null }.forEach { prop ->
                        prop.value.itemStack = getModel(prop.value.youkaiModelId!!).toPropItemStack()
                    }
                }
                ResourcepackManager.resend()
            }
        }
    }
}

@Serializable
data class YoukaiSync(
    val models: List<YoukaiServerModel>,
    val itemFont: Map<String, String>
)

@Serializable
data class YoukaiServerModel(
    val modelId: String,
    var customModelId: Int,
    val baseMaterial: String
)