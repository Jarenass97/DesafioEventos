package model

data class EventoItem(
    var nombre: String,
    var fecha: String,
    var hora: String,
    var asistentes: ArrayList<String>
) {
    fun numAsistentes(): Int = asistentes.size
}
