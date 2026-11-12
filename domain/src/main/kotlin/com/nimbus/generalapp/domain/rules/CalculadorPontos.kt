package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.TipoMarcacao

/**
 * Calcula pontos de uma marcação a partir do perfil.
 * Casas numéricas: quantidade × número (só NORMAL; DE_MAO é rejeitado).
 * General DE_MAO: sempre [PerfilDeRegras.valorGeneralDeMao], independente de bonusDeMaoExiste.
 * Full/Seguida/Quadra DE_MAO: valores de mão só se bonusDeMaoExiste.
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
        require(tipo != TipoMarcacao.DE_MAO) { "De mão não se aplica a casas numéricas" }
        require(quantidade in 0..5) { "Quantidade deve estar entre 0 e 5" }
        return quantidade * casa.numero!!
    }

    return when (casa) {
        Casa.FULL -> {
            val usarDeMao = tipo == TipoMarcacao.DE_MAO && perfil.bonusDeMaoExiste
            if (usarDeMao) perfil.valorFullDeMao else perfil.valorFullNormal
        }
        Casa.SEGUIDA -> {
            val usarDeMao = tipo == TipoMarcacao.DE_MAO && perfil.bonusDeMaoExiste
            if (usarDeMao) perfil.valorSeguidaDeMao else perfil.valorSeguidaNormal
        }
        Casa.QUADRA -> {
            val usarDeMao = tipo == TipoMarcacao.DE_MAO && perfil.bonusDeMaoExiste
            if (usarDeMao) perfil.valorQuadraDeMao else perfil.valorQuadraNormal
        }
        Casa.GENERAL -> {
            if (tipo == TipoMarcacao.DE_MAO) perfil.valorGeneralDeMao
            else perfil.valorGeneralNormal
        }
        else -> error("Casa inesperada: $casa")
    }
}
