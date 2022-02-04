package adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import assistant.CamposBD.ACTIVADO__USUARIOS
import assistant.CamposBD.COL_USUARIOS
import com.example.eventoscompartidos.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import model.UsuarioGestion

class GestionUsuariosAdapter(
    var context: AppCompatActivity,
    var usuarios: ArrayList<UsuarioGestion>
) :
    RecyclerView.Adapter<GestionUsuariosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(R.layout.gestion_usuarios_item, parent, false),
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

    class ViewHolder(view: View, val context: AppCompatActivity) :
        RecyclerView.ViewHolder(view) {
        val imgIsActivated = view.findViewById<ImageView>(R.id.imgIsActivo)
        val txtUsuario = view.findViewById<TextView>(R.id.txtUsuarioGestion)
        val db = Firebase.firestore

        @SuppressLint("NotifyDataSetChanged")
        fun bind(
            user: UsuarioGestion,
            context: AppCompatActivity,
            pos: Int,
            gestionUsuariosAdapter: GestionUsuariosAdapter
        ) {
            if (user.activado) imgIsActivated.apply {
                setColorFilter(R.color.green)
                setImageResource(R.drawable.ic_activo)
            }
            else imgIsActivated.apply {
                setColorFilter(R.color.red)
                setImageResource(R.drawable.ic_inactivo)
            }
            txtUsuario.text = user.email
            itemView.setOnClickListener(View.OnClickListener {
                user.activado = !user.activado
                db.collection(COL_USUARIOS).document(user.email)
                    .update(ACTIVADO__USUARIOS, user.activado)
                gestionUsuariosAdapter.notifyDataSetChanged()
            })
            itemView.setOnLongClickListener(View.OnLongClickListener {
                eliminar(user, gestionUsuariosAdapter)
                true
            })
        }

        private fun eliminar(user: UsuarioGestion, gestionUsuariosAdapter: GestionUsuariosAdapter) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.strEliminar))
                .setMessage(context.getString(R.string.strConfirmacionDeleteUser))
                .setPositiveButton(context.getString(R.string.strAceptar)) { view, _ ->
                    gestionUsuariosAdapter.delUser(user)
                    db.collection(COL_USUARIOS).document(user.email).delete()
                    //enviarEmail(user.email)
                    view.dismiss()
                }
                .setNegativeButton(context.getString(R.string.strCancelar)) { view, _ ->
                    view.dismiss()
                }
                .setCancelable(true)
                .create().show()
        }

        private fun enviarEmail(email: String) {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null)).apply {
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    context.getString(R.string.strAsuntoEmailCuentaEliminada)
                )
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.strTextEmailCuentaEliminada))
            }
            context.startActivity(Intent.createChooser(intent,"CUENTA ELIMINADA"))
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun delUser(user: UsuarioGestion) {
        usuarios.remove(user)
        notifyDataSetChanged()
    }
}