package com.example.eventoscompartidos.fragments.Administrador

import adapters.EventosAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.BDFirestore
import com.example.eventoscompartidos.R
import kotlinx.android.synthetic.main.fragment_gestion_eventos.*
import kotlinx.android.synthetic.main.fragment_menu_admin.*

class MenuInferiorAdminFragment(val ventana: AppCompatActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_admin, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAdminEvents.setOnClickListener {
            cargarGestionEventos()
        }
        btnAdminUsuarios.setOnClickListener {
            cargarGestionUsuarios()
        }
        btnPerfilAdmin.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> view.setBackgroundResource(R.color.itemSelected)
                MotionEvent.ACTION_UP -> {
                    view.setBackgroundResource(R.color.yellow_dark)
                    
                }
            }
            true
        }
    }

    private fun cargarGestionEventos() {
        ventana.title = "Gestión de eventos"
        val fragment = GestionEventosFragment(ventana)
        replaceFragmentVentana(fragment)
    }

    private fun cargarGestionUsuarios() {
        ventana.title = "Gestión de usuarios"
        val fragment = GestionUsuariosFragment(ventana)
        replaceFragmentVentana(fragment)
    }

    private fun replaceFragmentVentana(fragment: Fragment) {
        val fragmentTransaction = ventana.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frmVentana, fragment)
        fragmentTransaction.commit()
    }
}