package adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import assistant.Auxiliar
import assistant.BDFirestore
import com.example.eventoscompartidos.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UsuariosAinvitarAdapter(
    var context: AppCompatActivity,
    var usuarios: ArrayList<String>
) :
    RecyclerView.Adapter<UsuariosAinvitarAdapter.ViewHolder>() {

    companion object {
        var seleccionado: Int = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(R.layout.usuarios_item, parent, false),
            context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = usuarios[position]
        holder.bind(item, context, position, this)
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }

    fun getSelected(): String {
        val user = if (seleccionado != -1) usuarios[seleccionado] else ""
        deseleccionar()
        return user
    }

    fun deseleccionar() {
        seleccionado = -1
        notifyDataSetChanged()
    }

    class ViewHolder(view: View, val context: AppCompatActivity) :
        RecyclerView.ViewHolder(view) {
        val imgUsuario = view.findViewById<ImageView>(R.id.imgUsuarioItem)
        val txtNombre = view.findViewById<TextView>(R.id.txtNombreUsuarioItem)
        val storageRef = Firebase.storage.reference

        @SuppressLint("SetTextI18n")
        fun bind(
            emailUsuario: String,
            context: AppCompatActivity,
            pos: Int,
            adapter: UsuariosAinvitarAdapter
        ) {
            cargarImagen(emailUsuario)
            txtNombre.text = emailUsuario
            if (pos == seleccionado) {
                with(itemView) { setBackgroundColor(Color.GRAY) }
                with(txtNombre) { setTextColor(Color.WHITE) }
            } else {
                with(itemView) { setBackgroundColor(Color.WHITE) }
                with(txtNombre) { setTextColor(Color.BLACK) }
            }
            itemView.setOnClickListener {
                marcarSeleccion(pos, adapter)
            }
        }

        private fun marcarSeleccion(pos: Int, adapter: UsuariosAinvitarAdapter) {
            seleccionado = if (seleccionado == pos) {
                -1
            } else pos
            adapter.notifyDataSetChanged()
        }

        private fun cargarImagen(emailUsuario: String) {
            val imgRef = storageRef.child("${BDFirestore.CARPETA_IMAGENES}/$emailUsuario.jpg")
            imgRef.getBytes(1024 * 1024)
                .addOnSuccessListener { imgUsuario.setImageBitmap(Auxiliar.getBitmap(it)) }
        }

    }

}