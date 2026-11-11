package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuadraTest {

    private val perfil = PerfilDeRegras.regrasDaCasa()

    @Test
    fun quatro4EUm2FormamQuadraValidaCom40NormalE45DeMao() {
        val avaliacao = avaliarCombinacao(listOf(4, 4, 4, 4, 2), perfil)
        assertTrue(avaliacao.ehQuadra)
        assertEquals(40, calcularPontos(Casa.QUADRA, TipoMarcacao.NORMAL, perfil))
        assertEquals(45, calcularPontos(Casa.QUADRA, TipoMarcacao.DE_MAO, perfil))
    }

    @Test
    fun cincoDadosIguaisNaoValemComoQuadra() {
        val avaliacao = avaliarCombinacao(listOf(4, 4, 4, 4, 4), perfil)
        assertTrue(avaliacao.ehGeneral)
        assertFalse(avaliacao.ehQuadra)
    }

    @Test
    fun trincaComParNaoEQuadra() {
        assertFalse(avaliarCombinacao(listOf(4, 4, 4, 2, 2), perfil).ehQuadra)
    }
}
