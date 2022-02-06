package adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.eventoscompartidos.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import model.Evento
import model.EventoGestion

class GestionEventosAdapter(
    var context: AppCompatActivity,
    var eventos: ArrayList<EventoGestion>
) :
    RecyclerView.Adapter<GestionEventosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(R.layout.gestion_eventos_item, parent, false),
            context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = eventos[position]
        holder.bind(item, context, position, this)
    }

    override fun getItemCount(): Int {
        return eventos.size
    }

    class ViewHolder(view: View, val context: AppCompatActivity) :
        RecyclerView.ViewHolder(view) {
        val txtNombre = view.findViewById<TextView>(R.id.txtNombreEventoGestion)
        val txtNumAsistentes = view.findViewById<TextView>(R.id.txtNumAsistentesGestion)
        val txtFechaHora = view.findViewById<TextView>(R.id.txtFechaEventoGestion)
        val db = Firebase.firestore

        @SuppressLint("SetTextI18n")
        fun bind(
            evento: EventoGestion,
            context: AppCompatActivity,
            pos: Int,
            gestionEventosAdapter: GestionEventosAdapter
        ) {
            txtNombre.text = evento.nombre
            txtNumAsistentes.text =
                "${evento.numAsistentes} ${context.getString(R.string.strAsistentes)}"
            txtFechaHora.text = "${evento.fecha} ${evento.hora}"
        }

    }

}