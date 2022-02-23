package com.example.eventoscompartidos

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import assistant.Auxiliar
import assistant.Auxiliar.usuario
import assistant.BDFirebase
import kotlinx.android.synthetic.main.activity_comentario_creater.*
import kotlinx.android.synthetic.main.comentario_item.*
import kotlinx.android.synthetic.main.fragment_perfil_usuario.*
import model.Comentario
import model.Evento
import model.Lugar
import java.io.FileNotFoundException
import java.io.InputStream

class ComentarioCreaterActivity : AppCompatActivity() {
    lateinit var lugar: Lugar
    lateinit var evento: Evento
    var image: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comentario_creater)

        val bun: Bundle = intent.extras!!
        lugar = bun.getSerializable("lugar") as Lugar
        evento = bun.getSerializable("evento") as Evento
        title = getString(R.string.strNuevoComentario, lugar.nombre)
        imgNuevoComentario.isVisible = false
        btnElegirImagenComentario.setOnClickListener {
            cambiarFoto()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miSave -> asegurar()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun asegurar() {
        if (datosRellenos()) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.strConfirmar))
                .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                    guardarComentario()
                    view.dismiss()
                }
                .setNegativeButton(getString(R.string.strCancelar)) { view, _ ->
                    view.dismiss()
                }
                .setCancelable(true)
                .create()
                .show()
        } else Toast.makeText(this, getString(R.string.strComentarioIncompleto), Toast.LENGTH_SHORT)
            .show()
    }

    private fun datosRellenos(): Boolean = edComentario.text.isNotEmpty() && image != null

    private fun guardarComentario() {
        val comentario =
            Comentario(edComentario.text.toString(), lugar.idNextComment(), usuario.email)
        lugar.addComment(comentario, evento)
        BDFirebase.cambiarImageComment(image!!, comentario.id)
    }

    private fun cambiarFoto() {
        AlertDialog.Builder(this)
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
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(
                this,
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
                                contentResolver.openInputStream(
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
        this.image = image
        imgNuevoComentario.isVisible = true
        imgNuevoComentario.setImageBitmap(image)
    }
}