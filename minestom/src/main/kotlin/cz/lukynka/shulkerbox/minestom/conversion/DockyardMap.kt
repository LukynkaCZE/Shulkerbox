package cz.lukynka.shulkerbox.minestom.conversion

import cz.lukynka.shulkerbox.minestom.*
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance

fun MinestomShulkerboxMap.toMinestomMap(origin: Point, world: Instance): MinestomMap {
    val map = this
    val sm = map.map

    val boundingBoxes = mutableListOf<MinestomBoundingBox>()
    sm.bounds.forEach {
        val bound = it.value
        val dockyardBound = MinestomBoundingBox(
            bound.id,
            bound.origin.toLocation(origin, map),
            world,
            bound.size.toVector3d(),
            bound.meta
        )
        boundingBoxes.add(dockyardBound)
    }

    val points = mutableListOf<MinestomPoint>()
    sm.points.forEach {
        val point = it.value
        val dockyardPoint = MinestomPoint(
            point.id,
            point.location.toLocation(origin, map),
            world,
            point.yaw,
            point.pitch,
            point.type,
            point.meta,
            point.uid
        )
        points.add(dockyardPoint)
    }

    val props = mutableListOf<MinestomProp>()
    sm.props.forEach {
        val prop = it.value
        val dockyardProp = MinestomProp(
            prop.uid,
            prop.location.toLocation(origin, map),
            world,
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

    val minestomMap = MinestomMap(
        sm.id,
        sm.name,
        boundingBoxes,
        points,
        props,
        sm.size.toVector3d(),
        sm.meta,
        origin,
        world,
        schematic = map.schematic
    )
    return minestomMap
}