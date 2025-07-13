package cz.lukynka.shulkerbox.minestom.conversion

import cz.lukynka.shulkerbox.common.ShulkerboxVector
import net.minestom.server.coordinate.Vec

fun ShulkerboxVector.toVector3d(): Vec {
    return Vec(x, y, z)
}