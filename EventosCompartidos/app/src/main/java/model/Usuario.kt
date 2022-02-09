package model

import android.graphics.Bitmap
import assistant.Auxiliar

data class Usuario(
    var email: String,
    var rol: Rol,
    var activado: Boolean,
    var imagen: ByteArray? = byteArrayOf()
) {
    fun isAdmin(): Boolean = rol == Rol.ADMINISTRADOR
    fun tieneImagen(): Boolean = !imagen.contentEquals(byteArrayOf())
}
