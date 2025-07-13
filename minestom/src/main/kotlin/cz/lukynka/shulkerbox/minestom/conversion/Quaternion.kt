package cz.lukynka.shulkerbox.minestom.conversion

import cz.lukynka.shulkerbox.common.ShulkerboxQuaternionf

fun ShulkerboxQuaternionf.toQuaternion(): List<Float> {
    return listOf(x, y, z, w)
}