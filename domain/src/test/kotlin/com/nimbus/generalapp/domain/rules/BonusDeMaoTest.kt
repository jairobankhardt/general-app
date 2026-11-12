package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.TipoMarcacao
import org.junit.Assert.assertEquals
import org.junit.Test

class BonusDeMaoTest {

    private val perfil = PerfilDeRegras.regrasDaCasa()

    @Test
    fun fullSeguidaEQuadraDeMaoValemMaisCinco() {
        assertEquals(
            perfil.valorFullNormal + 5,
            calcularPontos(Casa.FULL, TipoMarcacao.DE_MAO, perfil),
        )
        assertEquals(
            perfil.valorSeguidaNormal + 5,
            calcularPontos(Casa.SEGUIDA, TipoMarcacao.DE_MAO, perfil),
        )
        assertEquals(
            perfil.valorQuadraNormal + 5,
            calcularPontos(Casa.QUADRA, TipoMarcacao.DE_MAO, perfil),
        )
    }

    @Test
    fun deMaoEmFullSeguidaEQuadraUsaValoresNormaisQuandoBonusDeMaoExisteFalse() {
        val regras = perfil.copy(bonusDeMaoExiste = false)
        assertEquals(
            regras.valorFullNormal,
            calcularPontos(Casa.FULL, TipoMarcacao.DE_MAO, regras),
        )
        assertEquals(
            regras.valorSeguidaNormal,
            calcularPontos(Casa.SEGUIDA, TipoMarcacao.DE_MAO, regras),
        )
        assertEquals(
            regras.valorQuadraNormal,
            calcularPontos(Casa.QUADRA, TipoMarcacao.DE_MAO, regras),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun deMaoEmCasaNumericaERejeitado() {
        calcularPontos(Casa.TRES, TipoMarcacao.DE_MAO, perfil, quantidade = 4)
    }
}
