package cz.lukynka.shulkerbox.dockyard.conversion

import cz.lukynka.shulkerbox.common.ShulkerboxVector
import cz.lukynka.shulkerbox.dockyard.DockyardShulkerboxMap
import io.github.dockyardmc.location.Location

fun ShulkerboxVector.toLocation(origin: Location, map: DockyardShulkerboxMap): Location {
    //Take into account world edit position fuckery
    val offsetOrigin = origin.add(map.map.schematicToOriginOffset.toVector3d())

    return Location(
        x + offsetOrigin.x,
        y + offsetOrigin.y,
        z + offsetOrigin.z,
        origin.world
    )
}