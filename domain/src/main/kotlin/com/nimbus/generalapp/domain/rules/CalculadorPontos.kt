package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.TipoMarcacao

/**
 * Calcula pontos de uma marcação a partir do perfil.
 * Casas numéricas: quantidade × número (sem bônus de mão).
 * Queimada: sempre 0.
 */
fun calcularPontos(
    casa: Casa,
    tipo: TipoMarcacao,
    perfil: PerfilDeRegras,
    quantidade: Int = 0,
): Int {
    if (tipo == TipoMarcacao.QUEIMADA) return 0

    if (casa.ehNumerica) {
        require(quantidade in 0..5) { "Quantidade deve estar entre 0 e 5" }
        return quantidade * casa.numero!!
    }

    val usarDeMao = tipo == TipoMarcacao.DE_MAO && perfil.bonusDeMaoExiste

    return when (casa) {
        Casa.FULL -> if (usarDeMao) perfil.valorFullDeMao else perfil.valorFullNormal
        Casa.SEGUIDA -> if (usarDeMao) perfil.valorSeguidaDeMao else perfil.valorSeguidaNormal
        Casa.QUADRA -> if (usarDeMao) perfil.valorQuadraDeMao else perfil.valorQuadraNormal
        Casa.GENERAL -> perfil.valorGeneralNormal
        else -> error("Casa inesperada: $casa")
    }
}

fun pontosCasaNumerica(numero: Int, quantidade: Int): Int {
    require(numero in 1..6)
    require(quantidade in 0..5)
    return quantidade * numero
}
