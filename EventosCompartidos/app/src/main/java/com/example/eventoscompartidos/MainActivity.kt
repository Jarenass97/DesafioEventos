package com.example.eventoscompartidos

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import assistant.Auxiliar
import assistant.Auxiliar.usuario
import com.example.eventoscompartidos.fragments.GestionUsuariosFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var fragment: Fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Guardado de datos para toda la aplicación en la sesión.
        val prefs: SharedPreferences.Editor? =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs?.putString("email", usuario.email)
        prefs?.apply()

        if (usuario.isAdmin()) cargarAdministrador()
        else cargarAppUsuario()

    }

    private fun cargarAppUsuario() {

    }

    private fun cargarAdministrador() {
        title = "Activación de usuarios"
        fragment = GestionUsuariosFragment(this)
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frmVentana, fragment)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val prefs: SharedPreferences.Editor? =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs?.clear() //Al cerrar sesión borramos los datos
        prefs?.apply()
        FirebaseAuth.getInstance().signOut()
    }
}