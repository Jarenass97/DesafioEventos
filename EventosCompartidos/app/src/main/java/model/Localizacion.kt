package model

import java.io.Serializable

data class Localizacion(var latitud: Double, var longitud: Double) : Serializable{
    companion object {
        fun getCampos(): ArrayList<String> {
            val campos = ArrayList<String>(0)
            for (l in Localizacion::class.java.declaredFields) {
                campos.add(l.name)
            }
            campos.remove(campos.last())
            return campos
        }
    }
}
