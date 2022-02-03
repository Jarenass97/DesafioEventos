package adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import assistant.Auxiliar
import com.example.eventoscompartidos.R
import model.Usuario

class GestionUsuariosAdapter(
    var context: AppCompatActivity,
    var usuarios: ArrayList<Usuario>
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

    class ViewHolder(view: View, val ventana: AppCompatActivity) :
        RecyclerView.ViewHolder(view) {
        val imgIsActivated = view.findViewById<ImageView>(R.id.imgIsActivo)
        val txtUsuario = view.findViewById<TextView>(R.id.txtUsuarioGestion)

        fun bind(
            user: Usuario,
            context: AppCompatActivity,
            pos: Int,
            gestionUsuariosAdapter: GestionUsuariosAdapter
        ) {
            if (user.isActivado()) imgIsActivated.apply {
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
                gestionUsuariosAdapter.notifyDataSetChanged()
            })
            itemView.setOnLongClickListener(View.OnLongClickListener {

                true
            })
        }
    }
}