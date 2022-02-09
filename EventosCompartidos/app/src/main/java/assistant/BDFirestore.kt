package assistant

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import model.*

object BDFirestore {
    val CARPETA_IMAGENES = "imgsUsuarios"
    val COL_USUARIOS = "usuarios"
    val EMAIL__USUARIOS = "email"
    val ROL__USUARIOS = "rol"
    val ACTIVADO__USUARIOS = "activado"
    val IMAGEN__USUARIOS = "imagen"

    val COL_EVENTOS = "eventos"
    val NOMBRE__EVENTOS = "nombre"
    val FECHA__EVENTOS = "fecha"
    val HORA__EVENTOS = "hora"
    val ASISTENTES__EVENTOS = "asistentes"
    val PUNTO_REUNION__EVENTOS = "punto de reunión"
    val PRESENTES__EVENTOS = "presentes"

    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore


    //************************ USUARIOS ************************
    fun getUsuario(email: String): Usuario? {
        var usuario: Usuario? = null
        runBlocking {
            val job: Job = launch {
                val data: DocumentSnapshot = queryUser(email)
                if (data.exists()) {
                    usuario = Usuario(
                        data.get(EMAIL__USUARIOS) as String,
                        Rol.valueOf(data.get(ROL__USUARIOS) as String),
                        data.get(ACTIVADO__USUARIOS) as Boolean
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
                            dc.document.get(ACTIVADO__USUARIOS) as Boolean
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

    //************************ EVENTOS ************************
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
            ASISTENTES__EVENTOS to ev.asistentes
        )
        db.collection(COL_EVENTOS).document(Auxiliar.idEvento(ev))
            .set(evento)
    }

    fun deleteEvento(idEvento: String) {
        db.collection(COL_EVENTOS).document(idEvento).delete()
    }

    fun getEvento(idEvento: String): Evento {
        var evento: Evento? = null
        runBlocking {
            val job: Job = launch {
                val data: DocumentSnapshot = queryEvento(idEvento)
                val listaAsistentes = data.get(ASISTENTES__EVENTOS) as ArrayList<HashMap<String, *>>
                val keys = Asistente.getCampos()
                val asistentes = ArrayList<Asistente>(0)
                for (a in listaAsistentes) {
                    asistentes.add(Asistente(a[keys[0]].toString(), a[keys[1]].toString()))
                }
                evento = Evento(
                    data.get(NOMBRE__EVENTOS) as String,
                    data.get(FECHA__EVENTOS) as String,
                    data.get(HORA__EVENTOS) as String,
                    data.get(PUNTO_REUNION__EVENTOS) as Localizacion?,
                    asistentes
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
        db.collection(COL_EVENTOS).document(Auxiliar.idEvento(evento))
            .update(ASISTENTES__EVENTOS, asistentes)
    }

}