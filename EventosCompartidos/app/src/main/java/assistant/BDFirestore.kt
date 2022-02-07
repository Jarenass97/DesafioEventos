package assistant

import android.annotation.SuppressLint
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import model.Evento
import model.EventoGestion

object BDFirestore {
    val COL_USUARIOS = "usuarios"
    val EMAIL__USUARIOS = "email"
    val ROL__USUARIOS = "rol"
    val ACTIVADO__USUARIOS = "activado"

    val COL_EVENTOS = "eventos"
    val NOMBRE__EVENTO = "nombre"
    val FECHA__EVENTO = "fecha"
    val HORA__EVENTO = "hora"
    val ASISTENTES__EVENTO = "asistentes"
    val LOC__EVENTO = "localizacion"
    val PRESENTES__EVENTO = "presentes"

    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore

    fun getEventos(): ArrayList<EventoGestion> {
        var eventos = ArrayList<EventoGestion>(0)
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryEventos() as QuerySnapshot
                for (dc: DocumentChange in data.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val evento = EventoGestion(
                            dc.document.get(NOMBRE__EVENTO).toString(),
                            dc.document.get(FECHA__EVENTO).toString(),
                            dc.document.get(HORA__EVENTO).toString(),
                            dc.document.get(ASISTENTES__EVENTO) as Long
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

    private fun ordenarEventos(eventos: ArrayList<EventoGestion>): ArrayList<EventoGestion> {
        val eventsSort = eventos.sortedBy { it.hora }.sortedBy {
            fechaLimpia(it.fecha)
        }.toMutableList() as ArrayList<EventoGestion>
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
            NOMBRE__EVENTO to ev.nombre,
            FECHA__EVENTO to ev.fecha,
            HORA__EVENTO to ev.hora,
            ASISTENTES__EVENTO to 0
        )
        db.collection(COL_EVENTOS).document(Auxiliar.idEvento(ev))
            .set(evento)
    }

    fun deleteEvento(idEvento: String) {
        db.collection(COL_EVENTOS).document(idEvento).delete()
    }
}