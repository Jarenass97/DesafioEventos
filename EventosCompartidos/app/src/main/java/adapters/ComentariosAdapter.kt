package adapters

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import assistant.Auxiliar.usuario
import assistant.BDFirebase
import com.example.eventoscompartidos.R
import model.Comentario
import model.Evento
import model.Lugar
import model.Usuario

class ComentariosAdapter(
    var context: AppCompatActivity,
    val evento: Evento,
    val lugar: Lugar,
    var comentarios: ArrayList<Comentario>
) :
    RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(R.layout.comentario_item, parent, false),
            context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = comentarios[position]
        holder.bind(item, context, position, this)
    }

    override fun getItemCount(): Int {
        return comentarios.size
    }

    class ViewHolder(view: View, val context: AppCompatActivity) :
        RecyclerView.ViewHolder(view) {
        var txtNombreUser = view.findViewById<TextView>(R.id.txtNomUserComment)
        var imgUser = view.findViewById<ImageView>(R.id.imgUserComment)
        val btnDelete = view.findViewById<ImageView>(R.id.btnDeleteComment)
        var imgComentario = view.findViewById<ImageView>(R.id.imgComment)
        var txtComment = view.findViewById<TextView>(R.id.txtTextoComment)


        @SuppressLint("SetTextI18n")
        fun bind(
            comentario: Comentario,
            context: AppCompatActivity,
            pos: Int,
            comentariosAdapter: ComentariosAdapter
        ) {
            val user = BDFirebase.getUsuario(comentario.usuario)
            if (user != null) {
                val img = user.img
                if (img != null) imgUser.setImageBitmap(img)
                txtNombreUser.text = if (!user.sinUsername()) user.username else user.email
            } else txtNombreUser.text = comentario.usuario
            imgComentario.setImageBitmap(BDFirebase.getImgComment(comentario))
            txtComment.text = comentario.comment
            if (comentario.usuario == usuario.email) {
                btnDelete.isVisible = true
                btnDelete.setOnClickListener {
                    eliminarComentario(comentario, comentariosAdapter)
                }
            } else btnDelete.isVisible = false
        }

        private fun eliminarComentario(
            comentario: Comentario,
            comentariosAdapter: ComentariosAdapter
        ) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.strEliminar))
                .setMessage(context.getString(R.string.strMsgEliminarLugar, comentario.comment))
                .setPositiveButton(context.getString(R.string.strAceptar)) { view, _ ->
                    comentariosAdapter.delete(comentario)
                    view.dismiss()
                }
                .setNegativeButton(context.getString(R.string.strCancelar)) { view, _ ->
                    view.dismiss()
                }
                .setCancelable(true)
                .create().show()
        }
    }

    private fun delete(comentario: Comentario) {
        comentarios.remove(comentario)
        lugar.delComment(comentario, evento)
        BDFirebase.deleteImageComment(comentario.id)
        notifyDataSetChanged()
    }

}