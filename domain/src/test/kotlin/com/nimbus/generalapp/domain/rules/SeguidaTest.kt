package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguidaTest {

    private val perfil = PerfilDeRegras.regrasDaCasa()

    @Test
    fun umACincoESeguidaValidaCom25NormalE30DeMao() {
        val avaliacao = avaliarCombinacao(listOf(1, 2, 3, 4, 5), perfil)
        assertTrue(avaliacao.ehSeguida)
        assertEquals(25, calcularPontos(Casa.SEGUIDA, TipoMarcacao.NORMAL, perfil))
        assertEquals(30, calcularPontos(Casa.SEGUIDA, TipoMarcacao.DE_MAO, perfil))
    }

    @Test
    fun doisASeisESeguidaValida() {
        assertTrue(avaliarCombinacao(listOf(2, 3, 4, 5, 6), perfil).ehSeguida)
    }

    @Test
    fun tresQuatroCincoSeisUmEmQualquerOrdemESeguidaValida() {
        assertTrue(avaliarCombinacao(listOf(3, 4, 5, 6, 1), perfil).ehSeguida)
        assertTrue(avaliarCombinacao(listOf(6, 1, 5, 3, 4), perfil).ehSeguida)
    }

    @Test
    fun umDoisTresQuatroSeisNaoESeguida() {
        assertFalse(avaliarCombinacao(listOf(1, 2, 3, 4, 6), perfil).ehSeguida)
    }

    @Test
    fun comOpcao34561DesligadaTresQuatroCincoSeisUmEInvalido() {
        val perfilSem = perfil.copy(seguidaAceita34561 = false)
        assertFalse(avaliarCombinacao(listOf(3, 4, 5, 6, 1), perfilSem).ehSeguida)
        assertTrue(avaliarCombinacao(listOf(1, 2, 3, 4, 5), perfilSem).ehSeguida)
    }
}
