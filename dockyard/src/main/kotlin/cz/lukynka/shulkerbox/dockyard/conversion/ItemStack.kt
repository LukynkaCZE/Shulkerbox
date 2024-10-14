package cz.lukynka.shulkerbox.dockyard.conversion

import PropItemStack
import io.github.dockyardmc.item.EnchantmentGlintOverrideItemComponent
import io.github.dockyardmc.item.ItemStack
import io.github.dockyardmc.registry.registries.ItemRegistry

fun PropItemStack.toItemStack(): ItemStack {
    val baseItem = ItemRegistry["minecraft:${material.lowercase()}"]
    val item = ItemStack(baseItem)
    if(enchanted) item.components.add(EnchantmentGlintOverrideItemComponent(true))
    item.customModelData.value = customModelData
    return item
}