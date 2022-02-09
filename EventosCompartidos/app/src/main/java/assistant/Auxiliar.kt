package assistant

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import model.Evento
import model.EventoItem
import model.Usuario
import java.io.ByteArrayOutputStream

object Auxiliar {
    lateinit var usuario: Usuario

    fun idEvento(evento: Evento): String =
        "${evento.nombre}-${evento.fecha}-${evento.hora}".replace("/", "").replace(" ","_")

    fun idEvento(evento: EventoItem): String =
        "${evento.nombre}-${evento.fecha}-${evento.hora}".replace("/", "").replace(" ","_")

    fun getBytes(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }

    fun getBitmap(image: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(image, 0, image.size)
    }
}