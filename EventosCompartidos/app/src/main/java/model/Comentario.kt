package model

import java.io.Serializable

data class Comentario(var comment: String, var id: String):Serializable {
    companion object {
        fun getCampos(): ArrayList<String> {
            val campos = ArrayList<String>(0)
            for (l in Comentario::class.java.declaredFields) {
                campos.add(l.name)
            }
            campos.remove(campos.last())
            return campos
        }
    }
}
