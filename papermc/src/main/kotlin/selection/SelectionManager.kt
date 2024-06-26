package selection

import ShulkerboxPaper
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import toMiniMessage

object SelectionManager {

    val selectionMap = mutableMapOf<Player, Selection>()

    val selectionItem = ItemStack(Material.LEAD, 1)
    val prefix = "<#884dff>Shulkerbox <dark_gray>|<gray>"

    init {
        selectionItem.editMeta {
            it.displayName("<green><underlined>Shulkerbox Selection Tool".toMiniMessage())
            it.lore(mutableListOf(
                " ".toMiniMessage(),
                "<reset><gray>Select two positions by <aqua>Right-Clicking <gray> and".toMiniMessage(),
                "<reset><aqua>Left-Clicking".toMiniMessage(),
                " ".toMiniMessage(),
            ))
            it.persistentDataContainer.set(ShulkerboxPaper.namespacedKey, PersistentDataType.BOOLEAN, true)
        }
    }

    fun remove(player: Player) {
        val selection = selectionMap[player]
        if(selection != null) {
            selection.boundingBoxEntity.remove()
            selectionMap.remove(player)
        }
    }

    fun create(player: Player, location: Location): Selection {
        val selection = if(!selectionMap.containsKey(player)) {
            val sel = Selection(location)
            sel.setSecondPoint(location)
            selectionMap[player] = sel
            sel

        } else selectionMap[player]
        return selection!!
    }
}