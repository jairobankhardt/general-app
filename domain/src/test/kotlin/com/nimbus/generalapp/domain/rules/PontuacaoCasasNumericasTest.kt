package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Test

class PontuacaoCasasNumericasTest {

    private val perfil = PerfilDeRegras.regrasDaCasa()

    @Test
    fun tresDados4NaCasaDo4Valem12() {
        assertEquals(
            12,
            calcularPontos(Casa.QUATRO, TipoMarcacao.NORMAL, perfil, quantidade = 3),
        )
    }

    @Test
    fun zeroDadosIguaisAoNumeroValem0ENaoSaoQueimada() {
        assertEquals(
            0,
            calcularPontos(Casa.QUATRO, TipoMarcacao.NORMAL, perfil, quantidade = 0),
        )
    }

    @Test
    fun cincoDados6NaCasaDo6Valem30() {
        assertEquals(
            30,
            calcularPontos(Casa.SEIS, TipoMarcacao.NORMAL, perfil, quantidade = 5),
        )
    }
}
