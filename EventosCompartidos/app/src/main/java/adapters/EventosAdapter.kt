package adapters

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.eventoscompartidos.GestionEventoDetalle
import com.example.eventoscompartidos.R
import model.EventoItem

class EventosAdapter(
    var context: AppCompatActivity,
    var eventos: ArrayList<EventoItem>
) :
    RecyclerView.Adapter<EventosAdapter.ViewHolder>() {

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

        @SuppressLint("SetTextI18n")
        fun bind(
            evento: EventoItem,
            context: AppCompatActivity,
            pos: Int,
            eventosAdapter: EventosAdapter
        ) {
            txtNombre.text = evento.nombre
            txtNumAsistentes.text =
                "${evento.numAsistentes()} ${context.getString(R.string.strAsistentes)}"
            txtFechaHora.text = "${evento.fecha} - ${evento.hora}"

            itemView.setOnClickListener {
                val intent = Intent(context, GestionEventoDetalle::class.java)
                intent.putExtra(context.getString(R.string.strEvento), Auxiliar.idEvento(evento))
                context.startActivity(intent)
            }
            itemView.setOnLongClickListener {
                eliminar(evento, eventosAdapter)
                true
            }
        }

        private fun eliminar(evento: EventoItem, eventosAdapter: EventosAdapter) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.strEliminar))
                .setMessage(context.getString(R.string.strMensajeEliminarEvento, evento.nombre))
                .setPositiveButton(context.getString(R.string.strAceptar)) { view, _ ->
                    BDFirestore.deleteEvento(Auxiliar.idEvento(evento))
                    eventosAdapter.delete(evento)
                    Toast.makeText(
                        context,
                        context.getString(R.string.strEventoEliminado),
                        Toast.LENGTH_SHORT
                    ).show()
                    view.dismiss()
                }
                .setNegativeButton(context.getString(R.string.strCancelar)) { view, _ ->
                    view.dismiss()
                }
                .setCancelable(true)
                .create().show()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun delete(evento: EventoItem) {
        eventos.remove(evento)
        notifyDataSetChanged()
    }

}