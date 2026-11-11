package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullTest {

    private val perfil = PerfilDeRegras.regrasDaCasa()

    @Test
    fun tres3EDois5FormamFullValidoCom30NormalE35DeMao() {
        val avaliacao = avaliarCombinacao(listOf(3, 3, 3, 5, 5), perfil)
        assertTrue(avaliacao.ehFull)
        assertEquals(30, calcularPontos(Casa.FULL, TipoMarcacao.NORMAL, perfil))
        assertEquals(35, calcularPontos(Casa.FULL, TipoMarcacao.DE_MAO, perfil))
    }

    @Test
    fun cincoDadosIguaisNaoValemComoFull() {
        val avaliacao = avaliarCombinacao(listOf(4, 4, 4, 4, 4), perfil)
        assertTrue(avaliacao.ehGeneral)
        assertFalse(avaliacao.ehFull)
    }

    @Test
    fun trincaComDoisDadosDiferentesNaoEFull() {
        val avaliacao = avaliarCombinacao(listOf(3, 3, 3, 5, 6), perfil)
        assertFalse(avaliacao.ehFull)
    }
}
