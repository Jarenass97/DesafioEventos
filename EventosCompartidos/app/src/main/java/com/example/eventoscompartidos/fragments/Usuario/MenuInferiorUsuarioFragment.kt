package com.example.eventoscompartidos.fragments.Usuario

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import assistant.Auxiliar.usuario
import com.example.eventoscompartidos.R
import com.example.eventoscompartidos.fragments.PerfilUsuarioFragment
import kotlinx.android.synthetic.main.fragment_menu_admin.*
import kotlinx.android.synthetic.main.fragment_menu_usuario.*

class MenuInferiorUsuarioFragment(val ventana: AppCompatActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_usuario, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnListEvents.setOnClickListener {
            cargarListadoEventos()
        }
        btnPerfilUsuario.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> view.setBackgroundResource(R.color.itemSelected)
                MotionEvent.ACTION_UP -> {
                    view.setBackgroundResource(R.color.yellow_dark)
                    cargarPerfil()
                }
            }
            true
        }
        if (usuario.img != null) imgUsuarioMenuUsuario.setImageBitmap(usuario.img)
    }

    private fun cargarPerfil() {
        ventana.title = getString(R.string.strPerfil)
        val fragment = PerfilUsuarioFragment(ventana,imgUsuarioMenuUsuario)
        replaceFragmentVentana(fragment)
    }

    private fun cargarListadoEventos() {
        ventana.title = getString(R.string.strListadoEventos)
        val fragment = ListadoEventosFragment(ventana)
        replaceFragmentVentana(fragment)
    }

    private fun replaceFragmentVentana(fragment: Fragment) {
        val fragmentTransaction = ventana.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frmVentana, fragment)
        fragmentTransaction.commit()
    }
}