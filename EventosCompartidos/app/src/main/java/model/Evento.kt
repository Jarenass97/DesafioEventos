package model

import assistant.Auxiliar
import assistant.Auxiliar.usuario
import assistant.BDFirebase
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Evento(
    var nombre: String,
    var fecha: String,
    var hora: String,
    var puntoReunion: Localizacion? = null,
    var asistentes: ArrayList<Asistente> = ArrayList(0),
    var lugares: ArrayList<Lugar> = ArrayList(0)
) : Serializable {
    fun localizacionPuntoReunion(): LatLng = LatLng(puntoReunion!!.latitud, puntoReunion!!.longitud)
    fun addAsistente(asistente: Asistente) {
        asistentes.add(asistente)
        BDFirebase.actualizarAsistentesEvento(this, asistentes)
    }

    fun listaAsistentes(): ArrayList<String> {
        val lista = ArrayList<String>(0)
        for (a in asistentes) lista.add(a.email)
        return lista
    }

    fun tieneAsistentes(): Boolean = asistentes.size > 0

    fun addPlace(lugar: Lugar) {
        lugares.add(lugar)
    }

    fun estoyApuntado(): Boolean {
        return asistentes.contains(Asistente(usuario.email))
    }

    fun sinPuntoReunion(): Boolean {
        return puntoReunion == null
    }

    fun delLugar(lugar: Lugar) {
        lugares.remove(lugar)
        BDFirebase.actualizarListaLugares(this)
    }

    fun modifyPlace(lugar: Lugar, newLugar: Lugar) {
        lugares.remove(lugar)
        lugares.add(newLugar)
    }
}

