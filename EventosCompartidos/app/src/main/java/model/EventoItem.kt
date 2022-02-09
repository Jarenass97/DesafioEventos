package model

data class EventoItem(
    var nombre: String,
    var fecha: String,
    var hora: String,
    var asistentes: ArrayList<Asistente>
) {
    fun numAsistentes(): Int = asistentes.size
}
