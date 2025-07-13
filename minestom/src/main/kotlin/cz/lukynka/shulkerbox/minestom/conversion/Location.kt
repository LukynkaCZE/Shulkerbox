package cz.lukynka.shulkerbox.minestom.conversion

import cz.lukynka.shulkerbox.common.ShulkerboxVector
import cz.lukynka.shulkerbox.minestom.MinestomShulkerboxMap
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec

fun ShulkerboxVector.toLocation(origin: Point, map: MinestomShulkerboxMap): Point {
    //Take into account world edit position fuckery
    val offsetOrigin = origin.add(map.map.schematicToOriginOffset.toVector3d())

    return Vec(
        x + offsetOrigin.x(),
        y + offsetOrigin.y(),
        z + offsetOrigin.z(),
    )
}