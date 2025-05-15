package cz.lukynka.shulkerbox.dockyard.conversion

import cz.lukynka.shulkerbox.common.ShulkerboxVector
import io.github.dockyardmc.maths.vectors.Vector3d
import io.github.dockyardmc.maths.vectors.Vector3f

fun ShulkerboxVector.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}

fun ShulkerboxVector.toVector3f(): Vector3f {
    return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
}