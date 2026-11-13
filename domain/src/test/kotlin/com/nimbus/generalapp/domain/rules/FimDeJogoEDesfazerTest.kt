package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.Cartela
import com.nimbus.generalapp.domain.model.Marcacao
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.StatusPartida
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FimDeJogoEDesfazerTest {

    private val perfil = PerfilDeRegras.regrasDaCasa()

    @Test
    fun jogoTerminaQuandoTodasAsCasasDeTodosOsJogadoresEstaoPreenchidas() {
        var partida = partidaQuaseCompleta(
            pontosAna = mapOf(Casa.UM to 5),
            pontosBob = mapOf(Casa.UM to 3),
            faltaAna = Casa.GENERAL,
            faltaBob = Casa.GENERAL,
        )
        assertEquals(StatusPartida.EM_ANDAMENTO, partida.status)

        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.NORMAL) // ana, 50
        assertEquals(StatusPartida.EM_ANDAMENTO, partida.status)

        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.NORMAL) // bob, 50
        assertEquals(StatusPartida.FINALIZADA, partida.status)
        assertFalse(partida.terminouPorGeneralDeMao)
    }

    @Test
    fun empateGeraMultiplosVencedores() {
        var partida = partidaQuaseCompleta(
            pontosAna = mapOf(Casa.UM to 10),
            pontosBob = mapOf(Casa.UM to 10),
            faltaAna = Casa.GENERAL,
            faltaBob = Casa.GENERAL,
        )
        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.NORMAL)
        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.NORMAL)

        assertEquals(StatusPartida.FINALIZADA, partida.status)
        assertEquals(listOf(ANA, BOB), partida.vencedores)
    }

    @Test
    fun desfazerRemoveUltimaMarcacaoERestauraJogadorDaVez() {
        var partida = Partida.iniciar(listOf(ANA, BOB), perfil)
        assertEquals(ANA, partida.jogadorDaVez)

        partida = partida.marcar(Casa.FULL, TipoMarcacao.NORMAL)
        assertEquals(BOB, partida.jogadorDaVez)
        assertEquals(30, partida.cartela(ANA).total())

        partida = partida.desfazer()
        assertEquals(ANA, partida.jogadorDaVez)
        assertTrue(partida.cartela(ANA).estaLivre(Casa.FULL))
        assertEquals(0, partida.cartela(ANA).total())
    }

    @Test
    fun desfazerMarcacaoDeGeneralDeMaoReabreAPartida() {
        var partida = Partida.iniciar(listOf(ANA, BOB), perfil)
        partida = partida.marcar(Casa.GENERAL, TipoMarcacao.DE_MAO)

        assertEquals(StatusPartida.FINALIZADA, partida.status)
        assertTrue(partida.terminouPorGeneralDeMao)

        partida = partida.desfazer()

        assertEquals(StatusPartida.EM_ANDAMENTO, partida.status)
        assertTrue(partida.vencedores.isEmpty())
        assertFalse(partida.terminouPorGeneralDeMao)
        assertEquals(ANA, partida.jogadorDaVez)
        assertTrue(partida.cartela(ANA).estaLivre(Casa.GENERAL))
    }

    /**
     * Monta partida em que cada jogador tem todas as casas preenchidas
     * exceto [faltaAna]/[faltaBob], com totais controlados via casa UM.
     */
    private fun partidaQuaseCompleta(
        pontosAna: Map<Casa, Int>,
        pontosBob: Map<Casa, Int>,
        faltaAna: Casa,
        faltaBob: Casa,
    ): Partida {
        fun cartelaCom(pontos: Map<Casa, Int>, falta: Casa): Cartela {
            var cartela = Cartela()
            for (casa in Casa.entries) {
                if (casa == falta) continue
                val valor = pontos[casa]
                cartela = if (valor != null) {
                    cartela.marcar(Marcacao(casa, TipoMarcacao.NORMAL, valor))
                } else {
                    cartela.marcar(Marcacao(casa, TipoMarcacao.QUEIMADA, 0))
                }
            }
            return cartela
        }

        return Partida(
            regras = perfil,
            ordemJogadores = listOf(ANA, BOB),
            cartelas = mapOf(
                ANA to cartelaCom(pontosAna, faltaAna),
                BOB to cartelaCom(pontosBob, faltaBob),
            ),
            indiceJogadorDaVez = 0,
        )
    }
}
