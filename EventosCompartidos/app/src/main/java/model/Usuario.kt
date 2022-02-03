package model

data class Usuario(var email: String, var activado: Boolean) {
    fun isActivado(): Boolean {
        return activado
    }
}
