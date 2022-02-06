package assistant

import model.Evento
import model.Usuario

object Auxiliar {
    lateinit var usuario: Usuario

    fun idEvento(evento: Evento): String {
        return "${evento.nombre}-${evento.fecha}-${evento.hora}".replace("/", "")
    }

    fun ordenarEventos(eventos: ArrayList<Evento>): ArrayList<Evento> {
        val eventsSort = eventos.sortedBy { it.hora }.sortedBy {
            fechaLimpia(it.fecha)
        }.toMutableList() as ArrayList<Evento>
        return eventsSort
    }

    private fun fechaLimpia(fecha: String): String {
        var fechaLimpia = ""
        for (c in fecha.split("/").reversed().toString()) {
            if (c.isDigit()) {
                fechaLimpia += c
            }
        }
        return fechaLimpia
    }
}