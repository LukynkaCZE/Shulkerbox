package youkai

import cz.lukynka.lkws.LightweightWebServer
import cz.lukynka.lkws.responses.Response
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class YoukaiIntegration {

    private val webServer = LightweightWebServer(6969)
    private val youkaiToken = ""

    private fun youkaiAuth(response: Response): Boolean {
        return response.requestHeaders["Authentication"] == youkaiToken
    }

    fun start() {

        webServer.post("/youkai-sync", ::youkaiAuth) {
            val json = Json { ignoreUnknownKeys = true }
            val models = json.decodeFromString<YoukaiSync>(it.requestBody)


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
    var customModelId: Int? = null,
    val baseMaterial: String
)