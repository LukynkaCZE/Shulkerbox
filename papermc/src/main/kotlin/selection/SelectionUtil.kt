package selection

import ShulkerboxPaper
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import toMiniMessage

object SelectionUtil {

    val selectionItem = ItemStack(Material.LEAD, 1)
    val prefix = "<#884dff>Shulkerbox <dark_gray>| <gray>"

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
}