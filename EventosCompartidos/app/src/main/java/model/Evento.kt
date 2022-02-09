package model

import assistant.BDFirestore
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Evento(
    var nombre: String,
    var fecha: String,
    var hora: String,
    var puntoReunion: Localizacion? = null,
    var asistentes: ArrayList<String> = ArrayList(0)
) {
    fun localizacionPuntoReunion(): LatLng = LatLng(puntoReunion!!.latitud, puntoReunion!!.longitud)
    fun addAsistente(emailAsistente: String) {
        asistentes.add(emailAsistente)
    }
}

