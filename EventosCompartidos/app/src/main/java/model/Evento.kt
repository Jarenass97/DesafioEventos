package model

import assistant.BDFirestore
import com.google.android.gms.maps.model.LatLng

data class Evento(
    var nombre: String,
    var fecha: String,
    var hora: String,
    var puntoReunion: Localizacion? = null,
    var asistentes: ArrayList<Asistente> = ArrayList(0)
) {
    fun localizacionPuntoReunion(): LatLng = LatLng(puntoReunion!!.latitud, puntoReunion!!.longitud)
    fun addAsistente(asistente: Asistente) {
        asistentes.add(asistente)
        BDFirestore.actualizarAsistentesEvento(this, asistentes)
    }

    fun listaAsistentes(): ArrayList<String> {
        val lista = ArrayList<String>(0)
        for (a in asistentes) lista.add(a.email)
        return lista
    }

    fun tieneAsistentes(): Boolean = asistentes.size > 0
}

