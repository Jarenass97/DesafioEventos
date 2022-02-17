package com.example.eventoscompartidos.fragments.Administrador

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import assistant.Auxiliar
import assistant.Auxiliar.usuario
import com.example.eventoscompartidos.R
import com.example.eventoscompartidos.fragments.PerfilUsuarioFragment
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
                    cargarPerfil()
                }
            }
            true
        }
        if (usuario.img != null) imgUsuarioMenu.setImageBitmap(usuario.img)
    }

    private fun cargarPerfil() {
        ventana.title = "Perfil"
        val fragment = PerfilUsuarioFragment(ventana)
        replaceFragmentVentana(fragment)
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