package com.example.eventoscompartidos

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import assistant.Auxiliar.usuario
import com.example.eventoscompartidos.fragments.Administrador.GestionEventosFragment
import com.example.eventoscompartidos.fragments.Administrador.GestionUsuariosFragment
import com.example.eventoscompartidos.fragments.Administrador.MenuInferiorAdminFragment
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

        if (usuario.isAdmin()) cargarAppAdmin()
        else cargarAppUsuario()
    }

    private fun cargarMenuUser() {

    }

    private fun cargarMenuAdmin() {
        fragment = MenuInferiorAdminFragment(this)
        replaceFragmentMenu(fragment)
    }


    private fun cargarAppAdmin() {
        title = "Gestión de eventos"
        fragment = GestionEventosFragment(this)
        replaceFragmentVentana(fragment)
        cargarMenuAdmin()
    }

    private fun cargarAppUsuario() {
        cargarMenuUser()
    }

    private fun replaceFragmentVentana(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frmVentana, fragment)
        fragmentTransaction.commit()
    }

    private fun replaceFragmentMenu(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frmMenu, fragment)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        cerrarSesion()
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.strCerrarSesion))
            .setMessage(getString(R.string.strMsgCerrarSesion))
            .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                super.onBackPressed()
                val prefs: SharedPreferences.Editor? =
                    getSharedPreferences(
                        getString(R.string.prefs_file),
                        Context.MODE_PRIVATE
                    ).edit()
                prefs?.clear()
                prefs?.apply()
                FirebaseAuth.getInstance().signOut()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strCancelar)) { view, _ ->
                view.dismiss()
            }
            .setCancelable(true)
            .create().show()
    }
}