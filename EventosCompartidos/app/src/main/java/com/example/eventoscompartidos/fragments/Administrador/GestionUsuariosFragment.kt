package com.example.eventoscompartidos.fragments.Administrador

import adapters.GestionUsuariosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.Auxiliar.usuario
import assistant.CamposBD.ACTIVADO__USUARIOS
import assistant.CamposBD.COL_USUARIOS
import assistant.CamposBD.EMAIL__USUARIOS
import com.example.eventoscompartidos.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_gestion_usuarios.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import model.UsuarioGestion

class GestionUsuariosFragment(val ventana: AppCompatActivity) : Fragment() {

    lateinit var adaptador: GestionUsuariosAdapter
    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gestion_usuarios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvGestionUsuarios.setHasFixedSize(true)
        rvGestionUsuarios.layoutManager = LinearLayoutManager(ventana)
        adaptador = GestionUsuariosAdapter(ventana, getUsers())
        rvGestionUsuarios.adapter = adaptador
    }

    private fun getUsers(): ArrayList<UsuarioGestion> {
        var usuarios = ArrayList<UsuarioGestion>(0)
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryUsuarios() as QuerySnapshot
                for (dc: DocumentChange in data.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val user = UsuarioGestion(
                            dc.document.get(EMAIL__USUARIOS).toString(),
                            dc.document.get(ACTIVADO__USUARIOS) as Boolean
                        )
                        usuarios.add(user)
                    }
                }
            }
            job.join()
        }
        return usuarios
    }

    private suspend fun queryUsuarios(): Any {
        return db.collection(COL_USUARIOS)
            .whereNotEqualTo(EMAIL__USUARIOS, usuario.email)
            .get()
            .await()
    }
}