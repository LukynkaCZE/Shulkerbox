package cz.lukynka.shulkerbox.dockyard.youkai

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import io.github.dockyardmc.commands.Commands
import io.github.dockyardmc.commands.StringArgument
import io.github.dockyardmc.inventory.give
import io.github.dockyardmc.item.ItemStack
import io.github.dockyardmc.item.clone
import io.github.dockyardmc.maths.randomFloat
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.ItemRegistry
import io.github.dockyardmc.sounds.playSound
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import kotlin.random.Random

object YoukaiPack {

    lateinit var syncUrl: String

    fun load(syncUrl: String) {
        YoukaiPack.syncUrl = syncUrl
    }

    val items: MutableMap<String, ItemStack> = mutableMapOf()
    val itemsFont: MutableMap<String, String> = mutableMapOf()

    fun getItem(id: String): ItemStack {
        val icon = items[id]?.clone()?.withMeta {
            lore.clear()
        }
        return (icon?.clone() ?: ItemStack(Items.BARRIER).withDisplayName("<red>NoYoukaiModel<gray>::<red>$id").withLore("<#ff8a95>No youkai model with id:", "<dark_gray>- <#ffd08a>$id")).clone()
    }

    fun getItemFont(id: String): String {
        val character = itemsFont[id] ?: return "???"
        val escaped = unicodeStringToChar(character)
        return "<font|youkai_items_2d>${escaped}<r>"
    }

    fun getItemFontOrNull(id: String): String? {
        val character = itemsFont[id] ?: return null
        val escaped = unicodeStringToChar(character)
        return "<font|youkai_items_2d>${escaped}"
    }

    fun getOrNull(id: String): ItemStack? {
        return items[id]?.clone()
    }

    fun unicodeStringToChar(unicodeString: String): Char {
        // Ensure the string is in the correct format (e.g., "uE001")
        if (unicodeString.length != 5 || !unicodeString.startsWith("u")) {
            throw IllegalArgumentException("Invalid Unicode string format")
        }

        // Parse the hexadecimal code point, skipping the leading "u"
        val codePoint = Integer.parseInt(unicodeString.substring(1), 16)

        // Convert the code point to a character
        return Character.toChars(codePoint)[0]
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
                items.clear()
                sync.models.forEach { model ->
                    var stack = ItemStack(ItemRegistry["minecraft:${model.baseMaterial}"])
                    if(model.customModelId != null) stack = stack.withCustomModelData(model.customModelId!!.toFloat())
                    items[model.modelId] = stack
                    log("Fetched ${model.modelId}!", LogType.DEBUG)
                }
                sync.itemFont.forEach { character ->
                    itemsFont[character.key] = character.value
                }
                log("Fetched ${sync.models.size} youkai models!", LogType.NETWORK)
            }
            .exceptionally {
                log(it as Exception)
                null
            }
    }


    private fun suggestYoukaiIds(player: Player): List<String> {
        return items.keys.toList()
    }

    fun registerCommands() {
        Commands.add("/youkai") {
            withPermission("es.admin")
            addSubcommand("get") {
                addArgument("id", StringArgument(), YoukaiPack::suggestYoukaiIds)
                execute {
                    val player = it.getPlayerOrThrow()
                    val id = getArgument<String>("id")
                    val item = getItem(id)
                    player.give(item)
                    player.sendMessage("<yellow>You have been given item with youkai id <orange>$id<yellow>!")
                    player.playSound(Sounds.ENTITY_ITEM_PICKUP, 1f, Random.randomFloat(0.8f, 1.3f))
                }
            }

            addSubcommand("fetch") {
                execute {
                    val player = it.getPlayerOrThrow()
                    player.sendMessage("<yellow>Updating youkai information..")
                    update()
                }
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
    var customModelId: Int? = null,
    val baseMaterial: String
)