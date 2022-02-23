package model

import android.graphics.Bitmap
import android.util.Log
import assistant.Auxiliar.usuario
import assistant.BDFirebase
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Lugar(
    var nombre: String,
    var localizacion: Localizacion,
    var comentarios: ArrayList<Comentario> = ArrayList(0)
) : Serializable {
    fun latLng(): LatLng = LatLng(localizacion.latitud, localizacion.longitud)
    fun idNextComment(): String {
        val num = if (comentarios.isEmpty()) {
            1
        } else {
            val lastCom = comentarios.sortedBy { it.id }.last().id.split('-')
            lastCom[1].toInt() + 1
        }
        return "$nombre-$num"
    }

    fun addComment(comentario: Comentario, evento: Evento) {
        val lug = thisPLace()
        comentarios.add(comentario)
        evento.modifyPlace(lug, this)
        BDFirebase.actualizarComentariosLugar(evento)
    }

    private fun thisPLace(): Lugar {
        val coments = ArrayList<Comentario>(0)
        for (c in comentarios) {
            coments.add(c)
        }
        return Lugar(nombre, localizacion, coments)
    }

    fun numComentarios(): Int = comentarios.size

    companion object {
        fun getCampos(): ArrayList<String> {
            val campos = ArrayList<String>(0)
            for (l in Lugar::class.java.declaredFields) {
                campos.add(l.name)
            }
            campos.remove(campos.last())
            return campos.reversed() as ArrayList<String>
        }
    }
}
