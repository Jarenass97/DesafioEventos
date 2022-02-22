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
import assistant.BDFirebase
import com.example.eventoscompartidos.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import model.Asistente
import model.Evento
import model.Lugar

class LugaresAdapter(
    var context: AppCompatActivity,
    var lugares: ArrayList<Lugar>,
    val evento: Evento
) :
    RecyclerView.Adapter<LugaresAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(R.layout.lugares_item, parent, false),
            context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lugares[position]
        holder.bind(item, context, position, this)
    }

    override fun getItemCount(): Int {
        return lugares.size
    }

    class ViewHolder(view: View, val context: AppCompatActivity) :
        RecyclerView.ViewHolder(view) {
        val txtNombre = view.findViewById<TextView>(R.id.txtNombreLugarItem)
        val imgEdit = view.findViewById<ImageView>(R.id.btnEditLocationLugarItem)

        @SuppressLint("SetTextI18n")
        fun bind(
            lugar: Lugar,
            context: AppCompatActivity,
            pos: Int,
            lugaresAdapter: LugaresAdapter
        ) {
            txtNombre.text = lugar.nombre
            itemView.setOnClickListener {
                Toast.makeText(context, lugar.nombre, Toast.LENGTH_SHORT).show()
            }
            imgEdit.setOnClickListener {
                Toast.makeText(context, "Editando ${lugar.nombre}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}