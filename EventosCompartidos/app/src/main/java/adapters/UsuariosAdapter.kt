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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import model.Asistente
import model.Evento

class UsuariosAdapter(
    var context: AppCompatActivity,
    var asistentes: ArrayList<Asistente>,
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
        val storageRef = Firebase.storage.reference

        @SuppressLint("SetTextI18n")
        fun bind(
            asistente: Asistente,
            context: AppCompatActivity,
            pos: Int,
            usuariosAdapter: UsuariosAdapter
        ) {
            cargarImagen(asistente)
            txtNombre.text = asistente.email
            itemView.setOnLongClickListener {
                expulsar(asistente, usuariosAdapter)
                true
            }
        }

        private fun cargarImagen(asistente: Asistente) {
            val imgRef = storageRef.child("${BDFirestore.CARPETA_IMAGENES}/${asistente.email}.jpg")
            imgRef.getBytes(1024 * 1024)
                .addOnSuccessListener { imgUsuario.setImageBitmap(Auxiliar.getBitmap(it)) }
        }

        private fun expulsar(asistente: Asistente, usuariosAdapter: UsuariosAdapter) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.strExpulsar))
                .setMessage(context.getString(R.string.strMsgExpulsarUsuario, asistente.email))
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
    private fun delete(asistente: Asistente) {
        asistentes.remove(asistente)
        BDFirestore.actualizarAsistentesEvento(evento, asistentes)
        notifyDataSetChanged()
    }

}