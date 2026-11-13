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
    fun generalDeMaoComCasaLivreEncerraComVitoriaDoJogadorERegistra1000() {
        var partida = Partida.iniciar(listOf(ANA, BOB), perfil)
        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.DE_MAO)

        assertEquals(StatusPartida.FINALIZADA, partida.status)
        assertEquals(listOf(ANA), partida.vencedores)
        assertTrue(partida.terminouPorGeneralDeMao)
        assertEquals(1000, partida.cartela(ANA).marcacao(Casa.GENERAL)?.pontos)
    }

    @Test
    fun generalDeMaoPontua1000ComGeneralDeMaoVenceNaHoraFalse() {
        val regras = perfil.copy(generalDeMaoVenceNaHora = false)
        var partida = Partida.iniciar(listOf(ANA, BOB), regras)
        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.DE_MAO)

        assertEquals(StatusPartida.EM_ANDAMENTO, partida.status)
        assertFalse(partida.terminouPorGeneralDeMao)
        assertTrue(partida.vencedores.isEmpty())
        assertEquals(1000, partida.cartela(ANA).marcacao(Casa.GENERAL)?.pontos)
        assertEquals(
            1000,
            calcularPontos(Casa.GENERAL, TipoMarcacao.DE_MAO, regras),
        )
    }

    @Test
    fun generalDeMaoPontua1000ComBonusDeMaoExisteFalse() {
        val regras = perfil.copy(bonusDeMaoExiste = false)
        assertEquals(
            1000,
            calcularPontos(Casa.GENERAL, TipoMarcacao.DE_MAO, regras),
        )

        var partida = Partida.iniciar(listOf(ANA, BOB), regras)
        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.DE_MAO)

        assertEquals(StatusPartida.FINALIZADA, partida.status)
        assertTrue(partida.terminouPorGeneralDeMao)
        assertEquals(1000, partida.cartela(ANA).marcacao(Casa.GENERAL)?.pontos)
    }

    @Test
    fun generalDeMaoComCasaDoGeneralOcupadaNaoVenceEPodeMarcarCasaNumericaLivre() {
        var partida = Partida.iniciar(listOf(ANA, BOB), perfil)
        // Ana queima o General; Bob joga e devolve a vez
        partida = partida.queimar(Casa.GENERAL)
        partida = partida.queimar(Casa.UM) // bob

        assertFalse(partida.cartela(ANA).estaLivre(Casa.GENERAL))
        assertTrue(partida.podeMarcarCincoIguaisNaCasaNumerica(4, ANA))

        partida = partida.marcarNumerica(Casa.QUATRO, quantidade = 5)

        assertEquals(StatusPartida.EM_ANDAMENTO, partida.status)
        assertTrue(partida.vencedores.isEmpty())
        assertFalse(partida.terminouPorGeneralDeMao)
        assertEquals(20, partida.cartela(ANA).marcacao(Casa.QUATRO)?.pontos)
    }

    @Test
    fun generalComCasaGeneralECasaNumericaOcupadasSoPermiteQueimarOutraCasaLivre() {
        var partida = Partida.iniciar(listOf(ANA, BOB), perfil)
        partida = partida.queimar(Casa.GENERAL) // ana
        partida = partida.queimar(Casa.UM) // bob
        partida = partida.marcarNumerica(Casa.QUATRO, quantidade = 2) // ana
        partida = partida.queimar(Casa.DOIS) // bob

        assertFalse(partida.podeMarcarCincoIguaisNaCasaNumerica(4, ANA))
        assertTrue(partida.cartela(ANA).estaLivre(Casa.FULL))

        partida = partida.queimar(Casa.FULL)

        assertEquals(0, partida.cartela(ANA).marcacao(Casa.FULL)?.pontos)
        assertEquals(TipoMarcacao.QUEIMADA, partida.cartela(ANA).marcacao(Casa.FULL)?.tipo)
    }
}
