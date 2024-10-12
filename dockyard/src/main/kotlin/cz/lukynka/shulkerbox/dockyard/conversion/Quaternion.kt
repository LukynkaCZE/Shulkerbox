package cz.lukynka.shulkerbox.dockyard.conversion

import ShulkerboxQuaternionf
import io.github.dockyardmc.utils.Quaternion

fun ShulkerboxQuaternionf.toQuaternion(): Quaternion {
    return Quaternion(x, y, z, w)
}