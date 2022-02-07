package assistant

import model.Evento
import model.EventoGestion
import model.Usuario

object Auxiliar {
    lateinit var usuario: Usuario

    fun idEvento(evento: Evento): String =
        "${evento.nombre}-${evento.fecha}-${evento.hora}".replace("/", "").replace(" ","_")

    fun idEvento(evento: EventoGestion): String =
        "${evento.nombre}-${evento.fecha}-${evento.hora}".replace("/", "").replace(" ","_")

}