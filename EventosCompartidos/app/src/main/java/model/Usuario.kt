package model

data class Usuario(var email: String, var rol: Rol, var activado: Boolean) {
    fun isActivado(): Boolean {
        return activado
    }
}
