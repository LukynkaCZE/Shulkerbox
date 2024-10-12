package cz.lukynka.shulkerbox.dockyard.conversion

import cz.lukynka.shulkerbox.dockyard.*
import io.github.dockyardmc.location.Location

fun DockyardShulkerboxMap.toDockyardMap(origin: Location): DockyardMap {
    val map = this
    val sm = map.map

    val boundingBoxes = mutableListOf<DockyardBoundingBox>()
    sm.bounds.forEach {
        val bound = it.value
        val dockyardBound = DockyardBoundingBox(
            bound.id,
            bound.origin.toLocation(origin, map),
            bound.size.toVector3d(),
            bound.meta
        )
        boundingBoxes.add(dockyardBound)
    }

    val points = mutableListOf<DockyardPoint>()
    sm.points.forEach {
        val point = it.value
        val dockyardPoint = DockyardPoint(
            point.id,
            point.location.toLocation(origin, map).apply { yaw = point.yaw; pitch = point.pitch },
            point.type,
            point.meta,
            point.uid
        )
        points.add(dockyardPoint)
    }

    val props = mutableListOf<DockyardProp>()
    sm.props.forEach {
        val prop = it.value
        val dockyardProp = DockyardProp(
            prop.uid,
            prop.location.toLocation(origin, map).apply { yaw = prop.yaw; pitch = prop.pitch },
            prop.yaw,
            prop.pitch,
            prop.meta,
            prop.transformation.translation.toVector3d(),
            prop.transformation.leftRotation.toQuaternion(),
            prop.transformation.scale.toVector3d(),
            prop.transformation.rightRotation.toQuaternion(),
            prop.brightness,
            prop.itemStack.toItemStack(),
            prop.youkaiModelId
        )
        props.add(dockyardProp)
    }

    val dockyardMap = DockyardMap(
        sm.id,
        sm.name,
        boundingBoxes,
        points,
        props,
        sm.size.toVector3d(),
        sm.meta,
        origin,
        schematic = map.schematic
    )
    return dockyardMap
}