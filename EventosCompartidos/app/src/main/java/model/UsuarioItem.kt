package model

data class UsuarioItem(var email: String, var activado: Boolean, var rol: Rol){
    fun isAdmin(): Boolean = rol == Rol.ADMINISTRADOR
}
