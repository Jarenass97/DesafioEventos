package model


data class Asistente(var email: String, var horaLlegada: String = ""){
    companion object {
        fun getCampos(): ArrayList<String> {
            val campos = ArrayList<String>(0)
            for (l in Asistente::class.java.declaredFields) {
                campos.add(l.name)
            }
            campos.remove(campos.last())
            return campos
        }
    }
}
