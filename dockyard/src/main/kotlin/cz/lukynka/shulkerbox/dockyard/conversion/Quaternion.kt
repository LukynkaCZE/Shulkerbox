package cz.lukynka.shulkerbox.dockyard.conversion

import cz.lukynka.shulkerbox.common.ShulkerboxQuaternionf
import io.github.dockyardmc.maths.Quaternion

fun ShulkerboxQuaternionf.toQuaternion(): Quaternion {
    return Quaternion(x, y, z, w)
}