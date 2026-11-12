package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.PerfilDeRegras

data class AvaliacaoCombinacao(
    val ehFull: Boolean,
    val ehSeguida: Boolean,
    val ehQuadra: Boolean,
    val ehGeneral: Boolean,
)

private val SEGUIDAS_PADRAO = setOf(
    setOf(1, 2, 3, 4, 5),
    setOf(2, 3, 4, 5, 6),
)

private val SEGUIDA_34561 = setOf(1, 3, 4, 5, 6)

/**
 * Avalia 5 dados (valores 1..6). Ordem irrelevante.
 * Cinco iguais só contam como Full/Quadra se o perfil permitir.
 */
fun avaliarCombinacao(dados: List<Int>, perfil: PerfilDeRegras): AvaliacaoCombinacao {
    require(dados.size == 5) { "É preciso exatamente 5 dados" }
    require(dados.all { it in 1..6 }) { "Cada dado deve estar entre 1 e 6" }

    val contagens = dados.groupingBy { it }.eachCount()
    val frequencias = contagens.values.sortedDescending()
    val ehGeneral = frequencias == listOf(5)
    val ehFullBase = frequencias == listOf(3, 2)
    val ehQuadraBase = frequencias == listOf(4, 1)

    val ehFull = when {
        ehFullBase -> true
        ehGeneral && perfil.cincoIguaisValemComoFull -> true
        else -> false
    }
    val ehQuadra = when {
        ehQuadraBase -> true
        ehGeneral && perfil.cincoIguaisValemComoQuadra -> true
        else -> false
    }

    val conjunto = dados.toSet()
    val ehSeguida = when {
        conjunto in SEGUIDAS_PADRAO -> true
        perfil.seguidaAceita34561 && conjunto == SEGUIDA_34561 -> true
        else -> false
    }

    return AvaliacaoCombinacao(
        ehFull = ehFull,
        ehSeguida = ehSeguida,
        ehQuadra = ehQuadra,
        ehGeneral = ehGeneral,
    )
}
