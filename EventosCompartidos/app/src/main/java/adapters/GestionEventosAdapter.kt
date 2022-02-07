package adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import assistant.Auxiliar
import assistant.BDFirestore
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
            txtFechaHora.text = "${evento.fecha} - ${evento.hora}"

            itemView.setOnLongClickListener {
                eliminar(evento, gestionEventosAdapter)
                true
            }
        }

        private fun eliminar(evento: EventoGestion, gestionEventosAdapter: GestionEventosAdapter) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.strEliminar))
                .setMessage(context.getString(R.string.strMensajeEliminarEvento))
                .setPositiveButton(context.getString(R.string.strAceptar)) { view, _ ->
                    BDFirestore.deleteEvento(Auxiliar.idEvento(evento))
                    gestionEventosAdapter.delete(evento)
                    Toast.makeText(context, context.getString(R.string.strEventoEliminado), Toast.LENGTH_SHORT).show()
                    view.dismiss()
                }
                .setNegativeButton(context.getString(R.string.strCancelar)){view,_->
                    view.dismiss()
                }
                .setCancelable(true)
                .create().show()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun delete(evento: EventoGestion) {
        eventos.remove(evento)
        notifyDataSetChanged()
    }

}