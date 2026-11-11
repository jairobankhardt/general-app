package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.StatusPartida
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneralTest {

    private val perfil = PerfilDeRegras.regrasDaCasa()

    @Test
    fun cinco5NormalValem50() {
        assertTrue(avaliarCombinacao(listOf(5, 5, 5, 5, 5), perfil).ehGeneral)
        assertEquals(50, calcularPontos(Casa.GENERAL, TipoMarcacao.NORMAL, perfil))
    }

    @Test
    fun generalDeMaoComCasaLivreEncerraComVitoriaDoJogador() {
        var partida = Partida.iniciar(listOf("ana", "bob"), perfil)
        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.DE_MAO)

        assertEquals(StatusPartida.FINALIZADA, partida.status)
        assertEquals(listOf("ana"), partida.vencedores)
        assertTrue(partida.terminouPorGeneralDeMao)
        assertEquals(50, partida.cartela("ana").marcacao(Casa.GENERAL)?.pontos)
    }

    @Test
    fun generalDeMaoComCasaDoGeneralOcupadaNaoVenceEPodeMarcarCasaNumericaLivre() {
        var partida = Partida.iniciar(listOf("ana", "bob"), perfil)
        // Ana queima o General; Bob joga e devolve a vez
        partida = partida.queimar(Casa.GENERAL)
        partida = partida.queimar(Casa.UM) // bob

        assertFalse(partida.cartela("ana").estaLivre(Casa.GENERAL))
        assertTrue(partida.podeMarcarCincoIguaisNaCasaNumerica(4, "ana"))

        partida = partida.marcarNumerica(Casa.QUATRO, quantidade = 5)

        assertEquals(StatusPartida.EM_ANDAMENTO, partida.status)
        assertTrue(partida.vencedores.isEmpty())
        assertFalse(partida.terminouPorGeneralDeMao)
        assertEquals(20, partida.cartela("ana").marcacao(Casa.QUATRO)?.pontos)
    }

    @Test
    fun generalComCasaGeneralECasaNumericaOcupadasSoPermiteQueimarOutraCasaLivre() {
        var partida = Partida.iniciar(listOf("ana", "bob"), perfil)
        partida = partida.queimar(Casa.GENERAL) // ana
        partida = partida.queimar(Casa.UM) // bob
        partida = partida.marcarNumerica(Casa.QUATRO, quantidade = 2) // ana
        partida = partida.queimar(Casa.DOIS) // bob

        assertFalse(partida.podeMarcarCincoIguaisNaCasaNumerica(4, "ana"))
        assertTrue(partida.cartela("ana").estaLivre(Casa.FULL))

        partida = partida.queimar(Casa.FULL)

        assertEquals(0, partida.cartela("ana").marcacao(Casa.FULL)?.pontos)
        assertEquals(TipoMarcacao.QUEIMADA, partida.cartela("ana").marcacao(Casa.FULL)?.tipo)
    }
}
