package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.Cartela
import com.nimbus.generalapp.domain.model.Marcacao
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class QueimarTest {

    @Test
    fun queimarMarcaZeroEmCasaLivreAQualquerMomento() {
        var partida = Partida.iniciar(listOf(ANA, BOB))
        partida = partida.queimar(Casa.SEGUIDA)

        val marcacao = partida.cartela(ANA).marcacao(Casa.SEGUIDA)!!
        assertEquals(TipoMarcacao.QUEIMADA, marcacao.tipo)
        assertEquals(0, marcacao.pontos)
    }

    @Test
    fun naoEPossivelMarcarCasaJaPreenchidaInclusiveQueimada() {
        val cartela = Cartela().marcar(
            Marcacao(Casa.FULL, TipoMarcacao.QUEIMADA, 0),
        )
        assertFalse(cartela.estaLivre(Casa.FULL))

        var falhou = false
        try {
            cartela.marcar(Marcacao(Casa.FULL, TipoMarcacao.NORMAL, 30))
        } catch (_: IllegalArgumentException) {
            falhou = true
        }
        assertEquals(true, falhou)

        var partida = Partida.iniciar(listOf(ANA, BOB))
        partida = partida.queimar(Casa.QUADRA)
        // bob joga para devolver a vez a ana
        partida = partida.queimar(Casa.UM)

        falhou = false
        try {
            partida.queimar(Casa.QUADRA)
        } catch (_: IllegalArgumentException) {
            falhou = true
        }
        assertEquals(true, falhou)
    }
}
