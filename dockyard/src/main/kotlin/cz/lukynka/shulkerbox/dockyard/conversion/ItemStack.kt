package cz.lukynka.shulkerbox.dockyard.conversion

import cz.lukynka.shulkerbox.common.PropItemStack
import io.github.dockyardmc.item.ItemStack
import io.github.dockyardmc.registry.registries.ItemRegistry

fun PropItemStack.toItemStack(): ItemStack {
    val baseItem = ItemRegistry["minecraft:${material.lowercase()}"]
    var item = ItemStack(baseItem)
    if(enchanted) item = item.withMeta { withEnchantmentGlint(true); withCustomModelData(customModelData.toFloat()) }
    return item
}