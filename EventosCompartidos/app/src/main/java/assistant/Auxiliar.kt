package assistant

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import model.Evento
import model.EventoItem
import model.Usuario
import java.io.ByteArrayOutputStream

object Auxiliar {
    val CODE_CHANGE_UBICATION = 1
    val CODE_PLACES = 2
    val LOCATION_REQUEST_CODE = 0
    val CODE_GALLERY = 1887
    val CODE_CAMERA = 1888

    lateinit var usuario: Usuario

    fun idEvento(evento: Evento): String =
        "${evento.nombre}-${evento.fecha}-${evento.hora}".replace("/", "").replace(" ", "_")

    fun idEvento(evento: EventoItem): String =
        "${evento.nombre}-${evento.fecha}-${evento.hora}".replace("/", "").replace(" ", "_")

    fun getBytes(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }

    fun getBitmap(image: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(image, 0, image.size)
    }

}