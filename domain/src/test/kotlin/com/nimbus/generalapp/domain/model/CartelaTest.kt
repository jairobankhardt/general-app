package com.nimbus.generalapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartelaTest {

    @Test
    fun totalECalculadoAPartirDasMarcacoesENuncaArmazenado() {
        var cartela = Cartela()
        cartela = cartela.marcar(Marcacao(Casa.FULL, TipoMarcacao.NORMAL, 30))
        cartela = cartela.marcar(Marcacao(Casa.UM, TipoMarcacao.NORMAL, 4))
        cartela = cartela.marcar(Marcacao(Casa.SEGUIDA, TipoMarcacao.QUEIMADA, 0))

        assertEquals(34, cartela.total())
        assertFalse(cartela.estaCompleta())
        assertTrue(cartela.estaLivre(Casa.GENERAL))
    }
}
