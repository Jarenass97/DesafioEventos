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
    var img: Bitmap? = null
) {
    init {
        runBlocking {
            val job: Job = launch {
                BDFirebase.getImg(email)
            }
            job.join()
        }
    }

    fun isAdmin(): Boolean = rol == Rol.ADMINISTRADOR
}
