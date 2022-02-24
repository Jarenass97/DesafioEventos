package model

import android.util.Log
import assistant.Auxiliar
import assistant.Auxiliar.usuario
import assistant.BDFirebase
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

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
        for(a in asistentes){
            if(a.email== usuario.email) return true
        }
        return false
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

    fun getLugar(lugar: Lugar): Lugar? {
        for (l in lugares) {
            if (l.nombre == lugar.nombre) return l
        }
        return null
    }

    fun delAsistente(email: String) {
        for (a in asistentes) {
            if (a.email == email){
                asistentes.remove(a)
                break
            }
        }
        BDFirebase.actualizarAsistentesEvento(this, asistentes)
    }

    fun indicarPresencialidad() {
        for (a in asistentes) {
            if (a.email == usuario.email) a.horaLlegada = getHoraActual()
        }
        BDFirebase.actualizarAsistentesEvento(this, asistentes)
    }

    private fun getHoraActual(): String {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        return "${String.format("%02d",hour)}:${String.format("%02d",minute)}"
    }

    fun estoyPresente(): Boolean {
        for (a in asistentes) {
            if (a.email == usuario.email)
                if (a.sinHoraLlegada()) return false
        }
        return true
    }
}

