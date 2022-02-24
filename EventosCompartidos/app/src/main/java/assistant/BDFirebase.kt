package assistant

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import assistant.Auxiliar.idEvento
import assistant.Auxiliar.usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import model.*
import java.lang.Exception

object BDFirebase {

    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore
    private val storageRef = Firebase.storage.reference

    //************************ USUARIOS ************************
    val CARPETA_IMAGENES = "FotosPerfil"
    val COL_USUARIOS = "usuarios"
    val EMAIL__USUARIOS = "email"
    val ROL__USUARIOS = "rol"
    val ACTIVADO__USUARIOS = "activado"
    val TIENE_FOTO__USUARIOS = "tieneFoto"
    val USERNAME__USUARIOS = "username"

    fun getUsuario(email: String): Usuario? {
        var usuario: Usuario? = null
        runBlocking {
            val job: Job = launch {
                val data: DocumentSnapshot = queryUser(email)
                if (data.exists()) {
                    usuario = Usuario(
                        data.get(EMAIL__USUARIOS) as String,
                        Rol.valueOf(data.get(ROL__USUARIOS) as String),
                        data.get(ACTIVADO__USUARIOS) as Boolean,
                        data.get(TIENE_FOTO__USUARIOS) as Boolean,
                        data.get(USERNAME__USUARIOS) as String
                    )
                }
            }
            job.join()
        }
        return usuario
    }

    private suspend fun queryUser(email: String): DocumentSnapshot {
        return db.collection(COL_USUARIOS)
            .document(email)
            .get()
            .await()
    }

    fun getUsers(): ArrayList<UsuarioItem> {
        var usuarios = ArrayList<UsuarioItem>(0)
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryUsuariosExceptMe() as QuerySnapshot
                for (dc: DocumentChange in data.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val user = UsuarioItem(
                            dc.document.get(EMAIL__USUARIOS).toString(),
                            dc.document.get(ACTIVADO__USUARIOS) as Boolean,
                            Rol.valueOf(dc.document.get(ROL__USUARIOS) as String)
                        )
                        usuarios.add(user)
                    }
                }
            }
            job.join()
        }
        return usuarios
    }

    private suspend fun queryUsuariosExceptMe(): Any {
        return db.collection(COL_USUARIOS)
            .whereNotEqualTo(EMAIL__USUARIOS, Auxiliar.usuario.email)
            .get()
            .await()
    }

    fun activarUsuario(user: UsuarioItem) {
        db.collection(COL_USUARIOS).document(user.email)
            .update(ACTIVADO__USUARIOS, true)
    }

    fun desactivarUsuario(user: UsuarioItem) {
        db.collection(COL_USUARIOS).document(user.email)
            .update(ACTIVADO__USUARIOS, false)
    }

    fun deleteUsuario(user: UsuarioItem) {
        db.collection(COL_USUARIOS).document(user.email).delete()
    }

    fun usuariosReg(): Boolean {
        var reg = false
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryUsuarios() as QuerySnapshot
                reg = data.documentChanges.size > 0
            }
            job.join()
        }
        return reg
    }

    private suspend fun queryUsuarios(): Any {
        return db.collection(COL_USUARIOS)
            .get()
            .await()
    }

    fun getUsuariosDisponibles(evento: Evento): ArrayList<String> {
        var usuarios = ArrayList<String>(0)
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryUsuariosDisponibles(evento) as QuerySnapshot
                for (dc: DocumentChange in data.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        usuarios.add(dc.document.get(EMAIL__USUARIOS).toString())
                    }
                }
            }
            job.join()
        }
        return usuarios
    }

    private suspend fun queryUsuariosDisponibles(evento: Evento): Any {
        return if (evento.tieneAsistentes()) db.collection(COL_USUARIOS)
            .whereNotIn(EMAIL__USUARIOS, evento.listaAsistentes())
            .get()
            .await()
        else db.collection(COL_USUARIOS)
            .get()
            .await()
    }

    fun cambiarImageUser(image: Bitmap) {
        val imgRef = storageRef.child("FotosPerfil/${usuario.email}.jpg")
        imgRef.putBytes(Auxiliar.getBytes(image)!!)
        db.collection(COL_USUARIOS).document(usuario.email)
            .update(TIENE_FOTO__USUARIOS, true)
    }

    fun getImg(email: String): Bitmap? {
        var img: Bitmap? = null
        runBlocking {
            val job: Job = launch {
                val data = image(email)
                if (data != null) img = Auxiliar.getBitmap(data)
            }
            job.join()
        }
        return img
    }

    private suspend fun image(email: String): ByteArray? {
        return try {
            val imgRef = storageRef.child("FotosPerfil/$email.jpg")
            val ONE_MEGABYTE: Long = 1024 * 1024
            imgRef.getBytes(ONE_MEGABYTE).await()
        } catch (e: Exception) {
            null
        }
    }

    fun changeUsername(nuevoNombre: String) {
        db.collection(COL_USUARIOS).document(usuario.email)
            .update(USERNAME__USUARIOS, nuevoNombre)
    }

    fun cambiarContraseña(newPass: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePassword(newPass)
    }

    fun cambiarEmail(newEmail: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updateEmail(newEmail)
        val emailActual = usuario.email
        db.collection(COL_USUARIOS).document(emailActual).get()
            .addOnSuccessListener {
                usuario.email = newEmail
                val data = it.data!!
                data[EMAIL__USUARIOS] = newEmail
                db.collection(COL_EVENTOS).document(newEmail).set(data)
                    .addOnSuccessListener {
                        db.collection(COL_EVENTOS).document(emailActual).delete()
                    }
            }
    }

    fun changeRol(user: UsuarioItem) {
        db.collection(COL_USUARIOS).document(user.email)
            .update(ROL__USUARIOS, user.rol)
    }

    //************************ EVENTOS ************************
    val COL_EVENTOS = "eventos"
    val NOMBRE__EVENTOS = "nombre"
    val FECHA__EVENTOS = "fecha"
    val HORA__EVENTOS = "hora"
    val ASISTENTES__EVENTOS = "asistentes"
    val PUNTO_REUNION__EVENTOS = "punto de reunión"
    val LUGARES__EVENTOS = "lugares"
    val PRESENTES__EVENTOS = "presentes"

    fun getEventos(): ArrayList<EventoItem> {
        var eventos = ArrayList<EventoItem>(0)
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryEventos() as QuerySnapshot
                for (dc: DocumentChange in data.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val evento = EventoItem(
                            dc.document.get(NOMBRE__EVENTOS).toString(),
                            dc.document.get(FECHA__EVENTOS).toString(),
                            dc.document.get(HORA__EVENTOS).toString(),
                            dc.document.get(ASISTENTES__EVENTOS) as ArrayList<Asistente>
                        )
                        eventos.add(evento)
                    }
                }
            }
        }
        eventos = ordenarEventos(eventos)
        return eventos
    }

    private suspend fun queryEventos(): Any {
        return db.collection(COL_EVENTOS)
            .get()
            .await()
    }

    private fun ordenarEventos(eventos: ArrayList<EventoItem>): ArrayList<EventoItem> {
        val eventsSort = eventos.sortedBy { it.hora }.sortedBy {
            fechaLimpia(it.fecha)
        }.toMutableList() as ArrayList<EventoItem>
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

    fun addEvento(ev: Evento) {
        val evento = hashMapOf(
            NOMBRE__EVENTOS to ev.nombre,
            FECHA__EVENTOS to ev.fecha,
            HORA__EVENTOS to ev.hora,
            ASISTENTES__EVENTOS to ev.asistentes,
            LUGARES__EVENTOS to ev.lugares
        )
        db.collection(COL_EVENTOS).document(Auxiliar.idEvento(ev))
            .set(evento)
    }

    fun deleteEvento(idEvento: String) {
        db.collection(COL_EVENTOS).document(idEvento)
            .get()
            .addOnSuccessListener {
                val lugares =
                    destriparLugares(it.get(LUGARES__EVENTOS) as ArrayList<HashMap<String, *>>)
                eliminarImagenes(lugares)
                db.collection(COL_EVENTOS).document(idEvento).delete()
            }
    }

    private fun eliminarImagenes(lugares: ArrayList<Lugar>) {
        for (l in lugares) {
            for (c in l.comentarios) {
                deleteImageComment(c.id)
            }
        }
    }

    fun getEvento(idEvento: String): Evento {
        var evento: Evento? = null
        runBlocking {
            val job: Job = launch {
                val data: DocumentSnapshot = queryEvento(idEvento)
                evento = Evento(
                    data.get(NOMBRE__EVENTOS) as String,
                    data.get(FECHA__EVENTOS) as String,
                    data.get(HORA__EVENTOS) as String,
                    destriparPuntoReunion(data.get(PUNTO_REUNION__EVENTOS) as HashMap<String, *>?),
                    destriparAsistentes(data.get(ASISTENTES__EVENTOS) as ArrayList<HashMap<String, *>>),
                    destriparLugares(data.get(LUGARES__EVENTOS) as ArrayList<HashMap<String, *>>)
                )
            }
            job.join()
        }
        return evento!!
    }

    private suspend fun queryEvento(idEvento: String): DocumentSnapshot {
        return db.collection(COL_EVENTOS)
            .document(idEvento)
            .get()
            .await()
    }

    private fun destriparLugares(data: ArrayList<HashMap<String, *>>): ArrayList<Lugar> {
        val keys = Lugar.getCampos()
        val lugares = ArrayList<Lugar>(0)
        for (d in data) {
            lugares.add(
                Lugar(
                    d[keys[0]] as String,
                    location(d[keys[1]] as HashMap<String, *>),
                    destriparComentarios(d)
                )
            )
        }
        return lugares
    }

    private fun destriparComentarios(d: HashMap<String, *>): ArrayList<Comentario> {
        val keys = Lugar.getCampos()
        val keysComments = Comentario.getCampos()
        val comentarios = ArrayList<Comentario>(0)
        for (c in d[keys[2]] as ArrayList<HashMap<String, *>>) {
            comentarios.add(
                Comentario(
                    c[keysComments[0]] as String,
                    c[keysComments[1]] as String,
                    c[keysComments[2]] as String
                )
            )
        }
        return comentarios
    }

    private fun location(loc: HashMap<String, *>): Localizacion {
        val keys = Localizacion.getCampos()
        return Localizacion(loc[keys[0]] as Double, loc[keys[1]] as Double)
    }

    private fun destriparPuntoReunion(loc: HashMap<String, *>?): Localizacion? {
        val keys = Localizacion.getCampos()
        return if (loc != null) Localizacion(loc[keys[0]] as Double, loc[keys[1]] as Double)
        else null
    }

    private fun destriparAsistentes(data: ArrayList<HashMap<String, *>>): ArrayList<Asistente> {
        val keys = Asistente.getCampos()
        val asistentes = ArrayList<Asistente>(0)
        for (a in data) {
            asistentes.add(Asistente(a[keys[0]].toString(), a[keys[1]].toString()))
        }
        return asistentes
    }

    fun changeNameEvent(evento: Evento, nuevoNombre: String) {
        val id = Auxiliar.idEvento(evento)
        db.collection(COL_EVENTOS).document(id).get()
            .addOnSuccessListener {
                evento.nombre = nuevoNombre
                val data = it.data!!
                data[NOMBRE__EVENTOS] = nuevoNombre
                db.collection(COL_EVENTOS).document(Auxiliar.idEvento(evento)).set(data)
                    .addOnSuccessListener {
                        db.collection(COL_EVENTOS).document(id).delete()
                    }
            }
    }

    fun changeDateEvent(evento: Evento, nuevaFecha: String) {
        val id = Auxiliar.idEvento(evento)
        db.collection(COL_EVENTOS).document(id).get()
            .addOnSuccessListener {
                evento.fecha = nuevaFecha
                val data = it.data!!
                data[FECHA__EVENTOS] = nuevaFecha
                db.collection(COL_EVENTOS).document(Auxiliar.idEvento(evento)).set(data)
                    .addOnSuccessListener {
                        db.collection(COL_EVENTOS).document(id).delete()
                    }
            }
    }

    fun changeHourEvent(evento: Evento, nuevaHora: String) {
        val id = Auxiliar.idEvento(evento)
        db.collection(COL_EVENTOS).document(id).get()
            .addOnSuccessListener {
                evento.hora = nuevaHora
                val data = it.data!!
                data[HORA__EVENTOS] = nuevaHora
                db.collection(COL_EVENTOS).document(Auxiliar.idEvento(evento)).set(data)
                    .addOnSuccessListener {
                        db.collection(COL_EVENTOS).document(id).delete()
                    }
            }
    }

    fun actualizarAsistentesEvento(evento: Evento, asistentes: ArrayList<Asistente>) {
        db.collection(COL_EVENTOS).document(idEvento(evento))
            .update(ASISTENTES__EVENTOS, asistentes)
    }

    fun establecerPuntoReunion(punto: Localizacion, evento: Evento) {
        db.collection(COL_EVENTOS).document(idEvento(evento))
            .update(PUNTO_REUNION__EVENTOS, punto)
    }

    fun actualizarListaLugares(evento: Evento) {
        db.collection(COL_EVENTOS).document(idEvento(evento))
            .update(LUGARES__EVENTOS, evento.lugares)
    }

    fun actualizarComentariosLugar(evento: Evento) {
        db.collection(COL_EVENTOS).document(idEvento(evento))
            .update(LUGARES__EVENTOS, evento.lugares)
    }

    fun cambiarImageComment(image: Bitmap, idComment: String) {
        val imgRef = storageRef.child("FotosComentarios/$idComment.jpg")
        imgRef.putBytes(Auxiliar.getBytes(image)!!)
    }

    fun getImgComment(comentario: Comentario): Bitmap {
        var img: Bitmap? = null
        runBlocking {
            val job: Job = launch {
                val data = imageComment(comentario.id)
                img = Auxiliar.getBitmap(data)
            }
            job.join()
        }
        return img!!
    }

    private suspend fun imageComment(idComment: String): ByteArray {
        val imgRef = storageRef.child("FotosComentarios/$idComment.jpg")
        val ONE_MEGABYTE: Long = 1024 * 1024
        return imgRef.getBytes(ONE_MEGABYTE).await()
    }

    fun deleteImageComment(idComment: String) {
        val imgRef = storageRef.child("FotosComentarios/$idComment.jpg")
        imgRef.delete()
    }

}