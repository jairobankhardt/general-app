package com.nimbus.generalapp.domain.rules

import com.nimbus.generalapp.domain.model.Casa
import com.nimbus.generalapp.domain.model.Cartela
import com.nimbus.generalapp.domain.model.JogadorId
import com.nimbus.generalapp.domain.model.Marcacao
import com.nimbus.generalapp.domain.model.PerfilDeRegras
import com.nimbus.generalapp.domain.model.StatusPartida
import com.nimbus.generalapp.domain.model.TipoMarcacao

data class EntradaHistorico(
    val jogadorId: JogadorId,
    val marcacao: Marcacao,
    val indiceJogadorDaVezAntes: Int,
    val statusAntes: StatusPartida,
    val vencedoresAntes: List<JogadorId>,
    val terminouPorGeneralDeMaoAntes: Boolean,
)

/**
 * Estado imutável de uma partida. O perfil é um snapshot das regras usadas.
 * Jogadores são referenciados só por [JogadorId].
 */
data class Partida(
    val regras: PerfilDeRegras,
    val ordemJogadores: List<JogadorId>,
    val cartelas: Map<JogadorId, Cartela>,
    val indiceJogadorDaVez: Int = 0,
    val historico: List<EntradaHistorico> = emptyList(),
    val status: StatusPartida = StatusPartida.EM_ANDAMENTO,
    val vencedores: List<JogadorId> = emptyList(),
    val terminouPorGeneralDeMao: Boolean = false,
) {
    init {
        require(ordemJogadores.size >= 2) { "A partida precisa de pelo menos 2 jogadores" }
        require(ordemJogadores.toSet().size == ordemJogadores.size) {
            "Jogadores duplicados na ordem"
        }
        require(cartelas.keys == ordemJogadores.toSet()) {
            "Cartelas devem corresponder exatamente aos jogadores"
        }
        require(indiceJogadorDaVez in ordemJogadores.indices)
    }

    val jogadorDaVez: JogadorId get() = ordemJogadores[indiceJogadorDaVez]

    fun cartela(jogadorId: JogadorId): Cartela =
        cartelas[jogadorId] ?: error("Jogador desconhecido: $jogadorId")

    companion object {
        fun iniciar(
            jogadores: List<JogadorId>,
            regras: PerfilDeRegras = PerfilDeRegras.regrasDaCasa(),
        ): Partida {
            require(jogadores.size >= 2)
            return Partida(
                regras = regras,
                ordemJogadores = jogadores,
                cartelas = jogadores.associateWith { Cartela() },
            )
        }
    }

    /**
     * Marca casa especial (Full, Seguida, Quadra, General) ou queima qualquer casa.
     * Para casas numéricas use [marcarNumerica].
     */
    fun marcar(
        casa: Casa,
        tipo: TipoMarcacao,
        jogadorId: JogadorId = jogadorDaVez,
    ): Partida {
        require(status == StatusPartida.EM_ANDAMENTO) { "Partida já finalizada" }
        require(jogadorId == jogadorDaVez) { "Não é a vez de $jogadorId" }
        require(!casa.ehNumerica || tipo == TipoMarcacao.QUEIMADA) {
            "Use marcarNumerica para casas numéricas (exceto queimar)"
        }

        val cartelaAtual = cartela(jogadorId)
        require(cartelaAtual.estaLivre(casa)) { "Casa $casa já está preenchida" }

        val pontos = calcularPontos(casa, tipo, regras)
        val marcacao = Marcacao(casa, tipo, pontos)
        return aplicarMarcacao(jogadorId, marcacao)
    }

    fun marcarNumerica(
        casa: Casa,
        quantidade: Int,
        jogadorId: JogadorId = jogadorDaVez,
    ): Partida {
        require(status == StatusPartida.EM_ANDAMENTO) { "Partida já finalizada" }
        require(jogadorId == jogadorDaVez) { "Não é a vez de $jogadorId" }
        require(casa.ehNumerica) { "Casa $casa não é numérica" }
        require(quantidade in 0..5)

        val cartelaAtual = cartela(jogadorId)
        require(cartelaAtual.estaLivre(casa)) { "Casa $casa já está preenchida" }

        val pontos = calcularPontos(casa, TipoMarcacao.NORMAL, regras, quantidade)
        val marcacao = Marcacao(casa, TipoMarcacao.NORMAL, pontos)
        return aplicarMarcacao(jogadorId, marcacao)
    }

    fun queimar(casa: Casa, jogadorId: JogadorId = jogadorDaVez): Partida =
        marcar(casa, TipoMarcacao.QUEIMADA, jogadorId)

    /**
     * Com General ocupado e cinco iguais: permite marcar a casa numérica da face,
     * se o perfil e a cartela permitirem.
     */
    fun podeMarcarCincoIguaisNaCasaNumerica(
        face: Int,
        jogadorId: JogadorId = jogadorDaVez,
    ): Boolean {
        if (!regras.cincoIguaisPodemIrParaCasaNumerica) return false
        val cartelaAtual = cartela(jogadorId)
        if (cartelaAtual.estaLivre(Casa.GENERAL)) return false
        val casaNumerica = Casa.numerica(face)
        return cartelaAtual.estaLivre(casaNumerica)
    }

    fun desfazer(): Partida {
        require(historico.isNotEmpty()) { "Não há marcação para desfazer" }
        val ultima = historico.last()
        val cartelaRestaurada = cartela(ultima.jogadorId).remover(ultima.marcacao.casa)
        return copy(
            cartelas = cartelas + (ultima.jogadorId to cartelaRestaurada),
            historico = historico.dropLast(1),
            indiceJogadorDaVez = ultima.indiceJogadorDaVezAntes,
            status = ultima.statusAntes,
            vencedores = ultima.vencedoresAntes,
            terminouPorGeneralDeMao = ultima.terminouPorGeneralDeMaoAntes,
        )
    }

    private fun aplicarMarcacao(jogadorId: JogadorId, marcacao: Marcacao): Partida {
        val entrada = EntradaHistorico(
            jogadorId = jogadorId,
            marcacao = marcacao,
            indiceJogadorDaVezAntes = indiceJogadorDaVez,
            statusAntes = status,
            vencedoresAntes = vencedores,
            terminouPorGeneralDeMaoAntes = terminouPorGeneralDeMao,
        )
        val novasCartelas = cartelas + (jogadorId to cartela(jogadorId).marcar(marcacao))
        val aposHistorico = copy(
            cartelas = novasCartelas,
            historico = historico + entrada,
        )

        // General de mão só é marcável com a casa livre; vitória imediata é regra fixa nesse caso.
        val generalDeMao =
            marcacao.casa == Casa.GENERAL &&
                marcacao.tipo == TipoMarcacao.DE_MAO &&
                regras.generalDeMaoVenceNaHora

        if (generalDeMao) {
            return aposHistorico.copy(
                status = StatusPartida.FINALIZADA,
                vencedores = listOf(jogadorId),
                terminouPorGeneralDeMao = true,
            )
        }

        if (novasCartelas.values.all { it.estaCompleta() }) {
            return aposHistorico.copy(
                status = StatusPartida.FINALIZADA,
                vencedores = determinarVencedores(novasCartelas),
                terminouPorGeneralDeMao = false,
            )
        }

        val proximo = (indiceJogadorDaVez + 1) % ordemJogadores.size
        return aposHistorico.copy(indiceJogadorDaVez = proximo)
    }

    private fun determinarVencedores(cartelasFinais: Map<JogadorId, Cartela>): List<JogadorId> {
        val totais = ordemJogadores.associateWith { cartelasFinais.getValue(it).total() }
        val maior = totais.values.maxOrNull() ?: return emptyList()
        return ordemJogadores.filter { totais.getValue(it) == maior }
    }
}
