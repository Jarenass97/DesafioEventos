package com.example.eventoscompartidos.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import assistant.Auxiliar
import assistant.Auxiliar.usuario
import assistant.BDFirebase
import com.example.eventoscompartidos.MainActivity
import com.example.eventoscompartidos.R
import kotlinx.android.synthetic.main.fragment_perfil_usuario.*
import model.Rol
import java.io.FileNotFoundException
import java.io.InputStream

class PerfilUsuarioFragment(val ventana: AppCompatActivity, val imgUsuarioMenu: ImageView) :
    Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCambiarImagenUsuario.setOnClickListener {
            cambiarFoto()
        }
        btnChangeUsername.setOnClickListener {
            cambiarUsername()
        }
        if (usuario.img != null) imgUsuarioPerfil.setImageBitmap(usuario.img)
        txtNombreUsuarioPerfil.text = getString(R.string.strUsername, usuario.username)
        if (usuario.isAdmin()) {
            rbAdmin.apply {
                text = Rol.ADMINISTRADOR.toString()
                isChecked = true
            }
            rbUser.text = Rol.USUARIO.toString()
            rgRoles.setOnCheckedChangeListener { radioGroup, id ->
                when (id) {
                    rbAdmin.id -> usuario.rol = Rol.ADMINISTRADOR
                    rbUser.id -> usuario.rol = Rol.USUARIO
                }
                cambiarRol()
            }
        } else lyRoles.isVisible = false
    }

    private fun cambiarRol() {
        val intent = Intent(ventana, MainActivity::class.java)
        startActivity(intent)
        ventana.finish()
    }

    private fun cambiarUsername() {
        val dialog = layoutInflater.inflate(R.layout.dialog_pide_string, null)
        val edNombre = dialog.findViewById<EditText>(R.id.edStringDialog)
        edNombre.setText(usuario.username)
        AlertDialog.Builder(ventana)
            .setTitle("Cambiar nombre")
            .setView(dialog)
            .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                val nuevoNombre = edNombre.text.toString()
                BDFirebase.changeUsername(nuevoNombre)
                usuario.username = nuevoNombre
                txtNombreUsuarioPerfil.text = getString(R.string.strUsername, usuario.username)
                Toast.makeText(ventana, getString(R.string.strSuccess), Toast.LENGTH_SHORT).show()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strCancelar)) { view, _ ->
                view.dismiss()
            }
            .setCancelable(true).create().show()
    }

    private fun cambiarFoto() {
        AlertDialog.Builder(ventana)
            .setTitle(getString(R.string.strElegirFoto))
            .setMessage(getString(R.string.strMensajeElegirFoto))
            .setPositiveButton(getString(R.string.strCamara)) { view, _ ->
                hacerFoto()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strGaleria)) { view, _ ->
                elegirDeGaleria()
                view.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun elegirDeGaleria() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Seleccione una imagen"),
            Auxiliar.CODE_GALLERY
        )
    }

    private fun hacerFoto() {
        if (ContextCompat.checkSelfPermission(
                ventana,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(
                ventana,
                arrayOf(Manifest.permission.CAMERA),
                Auxiliar.CODE_CAMERA
            )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Auxiliar.CODE_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Auxiliar.CODE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    var photo = data?.extras?.get("data") as Bitmap
                    cambiarImagen(photo)
                }
            }
            Auxiliar.CODE_GALLERY -> {
                if (resultCode === Activity.RESULT_OK) {
                    val selectedImage = data?.data
                    val selectedPath: String? = selectedImage?.path
                    if (selectedPath != null) {
                        var imageStream: InputStream? = null
                        try {
                            imageStream = selectedImage.let {
                                ventana.contentResolver.openInputStream(
                                    it
                                )
                            }
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                        val bmp = BitmapFactory.decodeStream(imageStream)
                        cambiarImagen(Bitmap.createScaledBitmap(bmp, 200, 300, true))
                    }
                }
            }
        }
    }


    private fun cambiarImagen(image: Bitmap) {
        BDFirebase.cambiarImageUser(image)
        imgUsuarioPerfil.setImageBitmap(image)
        imgUsuarioMenu.setImageBitmap(image)
        usuario.asignarImagen(image)
    }
}