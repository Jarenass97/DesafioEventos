package model

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Lugar(var nombre: String, var localizacion: Localizacion) : Serializable {
    fun latLng(): LatLng = LatLng(localizacion.latitud, localizacion.longitud)

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
