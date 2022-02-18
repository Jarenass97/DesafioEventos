package model

import android.graphics.Bitmap
import assistant.Auxiliar
import assistant.BDFirebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class Usuario(
    var email: String,
    var rol: Rol,
    var activado: Boolean,
    var tieneFoto: Boolean,
    var img: Bitmap? = if (tieneFoto) BDFirebase.getImg(email) else null
) {

    fun isAdmin(): Boolean = rol == Rol.ADMINISTRADOR
    fun asignarImagen(image: Bitmap) {
        img = image
        tieneFoto = true
    }
}
