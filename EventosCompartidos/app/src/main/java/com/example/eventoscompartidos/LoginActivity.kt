package com.example.eventoscompartidos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import assistant.Auxiliar
import assistant.BDFirestore.ACTIVADO__USUARIOS
import assistant.BDFirestore.COL_USUARIOS
import assistant.BDFirestore.EMAIL__USUARIOS
import assistant.BDFirestore.ROL__USUARIOS
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import model.Rol
import model.Usuario

class LoginActivity : AppCompatActivity() {
    private var RC_SIGN_IN = 1
    private val db = Firebase.firestore

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
                            acceder(consulta.result?.user?.email ?: "")
                        } else {
                            showAlert()
                        }
                    }
            } else Toast.makeText(this, getString(R.string.strCamposVacios), Toast.LENGTH_SHORT)
                .show()
        }

        btnReg.setOnClickListener() {
            if (edCorreo.text.isNotEmpty() && edPasswd.text.isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    edCorreo.text.toString(),
                    edPasswd.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        acceder(it.result?.user?.email ?: "")
                    } else {
                        showAlert()
                    }
                }
            } else Toast.makeText(this, getString(R.string.strCamposVacios), Toast.LENGTH_SHORT)
                .show()
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
            val usuario: Usuario = catchUser(email)!!
            irMain(usuario)
        }
    }

    //*******************************************************************************************
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential: AuthCredential =
                    GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            acceder(account.email ?: "")
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
    private fun acceder(email: String) {
        val usuario: Usuario? = catchUser(email)
        if (usuario == null) {
            registrarUsuario(email)
        } else {
            if (usuario.activado) {
                irMain(usuario)
                Toast.makeText(this, getString(R.string.strSuccess), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.strInactivo), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irMain(usuario: Usuario) {
        Auxiliar.usuario = usuario
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun catchUser(email: String): Usuario? {
        var usuario: Usuario? = null
        runBlocking {
            val job: Job = launch {
                val data: DocumentSnapshot = getUser(email)
                if (data.exists()) {
                    usuario = Usuario(
                        data.get(EMAIL__USUARIOS) as String,
                        Rol.valueOf(data.get(ROL__USUARIOS) as String),
                        data.get(ACTIVADO__USUARIOS) as Boolean
                    )
                }
            }
            job.join()
        }
        return usuario
    }

    private suspend fun getUser(email: String): DocumentSnapshot {
        return db.collection(COL_USUARIOS)
            .document(email)
            .get()
            .await()
    }

    private fun registrarUsuario(email: String) {
        var rol: Rol
        var act: Boolean
        val hayRegistrados = usuariosReg()
        rol = if (hayRegistrados) Rol.USUARIO
        else Rol.ADMINISTRADOR
        act = !hayRegistrados

        val user = hashMapOf(
            EMAIL__USUARIOS to email,
            ROL__USUARIOS to rol,
            ACTIVADO__USUARIOS to act
        )
        db.collection(COL_USUARIOS).document(email)
            .set(user)
            .addOnSuccessListener {
                if(hayRegistrados)
                Toast.makeText(this, getString(R.string.strWaitActivate), Toast.LENGTH_SHORT)
                    .show()
                else acceder(email)
            }
    }

    private fun usuariosReg(): Boolean {
        var reg = false
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryUsuarios() as QuerySnapshot
                reg = data.documentChanges.size > 0
            }
            job.join()
        }
        return reg
    }

    private suspend fun queryUsuarios(): Any {
        return db.collection(COL_USUARIOS)
            .get()
            .await()
    }
}
