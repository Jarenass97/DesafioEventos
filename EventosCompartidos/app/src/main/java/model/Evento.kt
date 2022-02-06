package model

import java.io.Serializable

data class Evento(
    var nombre: String,
    var fecha: String,
    var hora: String,
    var puntoReunion: Localizacion? = null,
)

