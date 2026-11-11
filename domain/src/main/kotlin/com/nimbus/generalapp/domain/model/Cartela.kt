package com.nimbus.generalapp.domain.model

data class Cartela(
    private val marcacoes: Map<Casa, Marcacao> = emptyMap(),
) {
    fun marcacao(casa: Casa): Marcacao? = marcacoes[casa]

    fun todasMarcacoes(): Map<Casa, Marcacao> = marcacoes

    fun estaLivre(casa: Casa): Boolean = casa !in marcacoes

    fun estaCompleta(): Boolean = Casa.entries.all { it in marcacoes }

    /** Total sempre derivado das marcações — nunca armazenado. */
    fun total(): Int = marcacoes.values.sumOf { it.pontos }

    fun marcar(marcacao: Marcacao): Cartela {
        require(estaLivre(marcacao.casa)) {
            "Casa ${marcacao.casa} já está preenchida"
        }
        return copy(marcacoes = marcacoes + (marcacao.casa to marcacao))
    }

    fun remover(casa: Casa): Cartela {
        require(casa in marcacoes) { "Casa $casa não está preenchida" }
        return copy(marcacoes = marcacoes - casa)
    }
}
