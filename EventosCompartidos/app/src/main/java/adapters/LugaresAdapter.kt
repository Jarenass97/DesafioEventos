package adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import assistant.Auxiliar
import com.example.eventoscompartidos.MapsActivity
import com.example.eventoscompartidos.R
import model.Comentario
import model.Evento
import model.Lugar
import model.MapsOptions

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
        val btnComments = view.findViewById<LinearLayout>(R.id.btnComentariosLugar)
        val txtNumComments = view.findViewById<TextView>(R.id.txtNumComentsLugarItem)

        @SuppressLint("SetTextI18n")
        fun bind(
            lugar: Lugar,
            context: AppCompatActivity,
            pos: Int,
            lugaresAdapter: LugaresAdapter
        ) {
            txtNombre.text = lugar.nombre
            txtNumComments.text = lugar.numComentarios().toString()
            itemView.setOnClickListener {
                modifyPlace(lugar, lugaresAdapter)
            }
            itemView.setOnLongClickListener {
                eliminarLugar(lugar, lugaresAdapter)
                true
            }
            btnComments.setOnClickListener {
                lugar.addComment(
                    Comentario("comentario de prueba", lugar.idNextComment()),
                    lugaresAdapter.evento
                )
            }
        }

        private fun modifyPlace(lugar: Lugar, lugaresAdapter: LugaresAdapter) {
            val intent = Intent(context, MapsActivity::class.java)
            intent.putExtra("opcion", MapsOptions.MODIFY_PLACE)
            intent.putExtra("lugar", lugar)
            intent.putExtra("evento", lugaresAdapter.evento)
            context.startActivityForResult(intent,Auxiliar.CODE_PLACES)
        }

        private fun eliminarLugar(lugar: Lugar, lugaresAdapter: LugaresAdapter) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.strEliminar))
                .setMessage(context.getString(R.string.strMsgEliminarLugar, lugar.nombre))
                .setPositiveButton(context.getString(R.string.strAceptar)) { view, _ ->
                    lugaresAdapter.delete(lugar)
                    view.dismiss()
                }
                .setNegativeButton(context.getString(R.string.strCancelar)) { view, _ ->
                    view.dismiss()
                }
                .setCancelable(true)
                .create().show()
        }
    }

    private fun delete(lugar: Lugar) {
        lugares.remove(lugar)
        evento.delLugar(lugar)
        notifyDataSetChanged()
    }

}