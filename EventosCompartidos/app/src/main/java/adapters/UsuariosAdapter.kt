package adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import assistant.Auxiliar
import assistant.BDFirestore
import com.example.eventoscompartidos.R
import model.Evento

class UsuariosAdapter(
    var context: AppCompatActivity,
    var asistentes: ArrayList<String>,
    val evento: Evento
) :
    RecyclerView.Adapter<UsuariosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(R.layout.usuarios_item, parent, false),
            context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = asistentes[position]
        holder.bind(item, context, position, this)
    }

    override fun getItemCount(): Int {
        return asistentes.size
    }

    class ViewHolder(view: View, val context: AppCompatActivity) :
        RecyclerView.ViewHolder(view) {
        val imgUsuario = view.findViewById<ImageView>(R.id.imgUsuarioItem)
        val txtNombre = view.findViewById<TextView>(R.id.txtNombreUsuarioItem)

        @SuppressLint("SetTextI18n")
        fun bind(
            asistente: String,
            context: AppCompatActivity,
            pos: Int,
            usuariosAdapter: UsuariosAdapter
        ) {
            txtNombre.text = asistente
            itemView.setOnLongClickListener {
                expulsar(asistente, usuariosAdapter)
                true
            }
        }

        private fun expulsar(asistente: String, usuariosAdapter: UsuariosAdapter) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.strExpulsar))
                .setMessage(context.getString(R.string.strMsgExpulsarUsuario, asistente))
                .setPositiveButton(context.getString(R.string.strAceptar)) { view, _ ->
                    usuariosAdapter.delete(asistente)
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
    private fun delete(asistente: String) {
        asistentes.remove(asistente)
        BDFirestore.actualizarAsistentesEvento(evento, asistentes)
        notifyDataSetChanged()
    }

}