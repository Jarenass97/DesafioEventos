package com.example.eventoscompartidos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var RC_SIGN_IN = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        title = getString(R.string.strTituloAutenticacion)

        //Con esto lanzamos eventos personalizados a GoogleAnalytics que podemos ver en nuestra consola de FireBase.
        val analy: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración completada")
        analy.logEvent("InitScreen", bundle)

        btnLogin.setOnClickListener() {
            if (edCorreo.text.isNotEmpty() && edPasswd.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(edCorreo.text.toString(), edPasswd.text.toString())
                    .addOnCompleteListener { consulta ->
                        if (consulta.isSuccessful) {
                            irMain(consulta.result?.user?.email ?: "")
                        } else {
                            showAlert()
                        }
                    }
            }else Toast.makeText(this, getString(R.string.strCamposVacios), Toast.LENGTH_SHORT).show()
        }

        btnReg.setOnClickListener() {
            if (edCorreo.text.isNotEmpty() && edPasswd.text.isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    edCorreo.text.toString(),
                    edPasswd.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        irMain(it.result?.user?.email ?: "")
                    } else {
                        showAlert()
                    }
                }
            }else Toast.makeText(this, getString(R.string.strCamposVacios), Toast.LENGTH_SHORT).show()
        }


        btnGoogle.setOnClickListener {
            //Configuración
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.request_id_token)) //Esto se encuentra en el archivo google-services.json: client->oauth_client -> client_id
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(
                this,
                googleConf
            ) //Este será el cliente de autenticación de Google.
            googleClient.signOut() //Con esto salimos de la posible cuenta  de Google que se encuentre logueada.
            val signInIntent = googleClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        session()
    }

    //******************************** Para la sesión ***************************
    private fun session() {
        val prefs: SharedPreferences = getSharedPreferences(
            getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        ) //Aquí no invocamos al edit, es solo para comprobar si tenemos datos en sesión.
        val email: String? = prefs.getString("email", null)
        if (email != null) {
            //Tenemos iniciada la sesión.
            irMain(email)
        }
    }

    //*******************************************************************************************
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la respuesta de esta activity se corresponde con la inicializada es que viene de la autenticación de Google.
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                //Ya tenemos la id de la cuenta. Ahora nos autenticamos con FireBase.
                val credential: AuthCredential =
                    GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            irMain(account.email ?: "")
                        } else {
                            showAlert()
                        }
                    }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                showAlert()
            }
        }
    }

    //*********************************************************************************
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.strError))
        builder.setMessage(getString(R.string.strMensajeErrorAuth))
        builder.setPositiveButton(getString(R.string.strAceptar), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //*********************************************************************************
    private fun irMain(email: String) {
        Toast.makeText(this, "Accediendo $email", Toast.LENGTH_SHORT).show()
        /*val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)*/
    }
}
