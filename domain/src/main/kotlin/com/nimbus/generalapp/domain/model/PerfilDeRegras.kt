package com.nimbus.generalapp.domain.model

/**
 * Snapshot configurável das regras. Valores padrão = "Regras da casa".
 * Toda pontuação variável deve vir daqui — nunca de constantes no motor.
 */
data class PerfilDeRegras(
    val nome: String = NOME_PADRAO,
    val valorFullNormal: Int = 30,
    val valorFullDeMao: Int = 35,
    val valorSeguidaNormal: Int = 25,
    val valorSeguidaDeMao: Int = 30,
    val valorQuadraNormal: Int = 40,
    val valorQuadraDeMao: Int = 45,
    val valorGeneralNormal: Int = 50,
    val valorGeneralDeMao: Int = 1000,
    val seguidaAceita34561: Boolean = true,
    val generalDeMaoVenceNaHora: Boolean = true,
    val cincoIguaisValemComoFull: Boolean = false,
    val cincoIguaisValemComoQuadra: Boolean = false,
    val cincoIguaisPodemIrParaCasaNumerica: Boolean = true,
    val bonusDeMaoExiste: Boolean = true,
) {
    companion object {
        const val NOME_PADRAO = "Regras da casa"

        fun regrasDaCasa(): PerfilDeRegras = PerfilDeRegras()
    }
}
