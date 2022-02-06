package model

import android.graphics.Bitmap
import java.io.Serializable

data class Lugar(var localizacion:Localizacion,var fotos:ArrayList<Bitmap>):Serializable
