package cz.lukynka.shulkerbox.dockyard.conversion

import ShulkerboxVector
import cz.lukynka.shulkerbox.dockyard.DockyardShulkerboxMap
import io.github.dockyardmc.extentions.broadcastMessage
import io.github.dockyardmc.location.Location

fun ShulkerboxVector.toLocation(origin: Location, map: DockyardShulkerboxMap): Location {
    io.github.dockyardmc.DockyardServer.broadcastMessage("adding ${this.toVector3d()} to $origin")
    //Take into account world edit position fuckery
    val offsetOrigin = origin.add(map.map.schematicToOriginOffset.toVector3d())

    return Location(
        x + offsetOrigin.x,
        y + offsetOrigin.y,
        z + offsetOrigin.z,
        origin.world
    )
}