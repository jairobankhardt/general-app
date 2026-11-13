package com.nimbus.generalapp.domain.model

/**
 * Cadastro imutável de jogadores. Operações devolvem resultado tipado —
 * erros de usuário nunca viram exceção.
 */
data class CadastroDeJogadores(
    private val jogadores: Map<JogadorId, Jogador> = emptyMap(),
) {
    fun ativos(): List<Jogador> =
        jogadores.values.filter { !it.arquivado }.sortedBy { it.nomeNormalizado }

    fun porId(id: JogadorId): Jogador? = jogadores[id]

    fun porNomeNormalizado(nome: String): Jogador? {
        val chave = normalizarNome(nome)
        return jogadores.values.firstOrNull { it.nomeNormalizado == chave }
    }

    fun adicionar(
        id: JogadorId,
        nome: String,
        cor: Int,
        convidado: Boolean = false,
    ): ResultadoAdicionarJogador {
        validarNome(nome)?.let { return it.toAdicionar() }
        conflitoDeNome(nome)?.let { return ResultadoAdicionarJogador.NomeDuplicado(it) }

        val jogador = Jogador(
            id = id,
            nome = prepararNome(nome),
            cor = cor,
            arquivado = false,
            convidado = convidado,
        )
        return ResultadoAdicionarJogador.Sucesso(
            cadastro = copy(jogadores = jogadores + (id to jogador)),
            jogador = jogador,
        )
    }

    fun renomear(id: JogadorId, novoNome: String): ResultadoRenomearJogador {
        val atual = jogadores[id]
            ?: return ResultadoRenomearJogador.JogadorNaoEncontrado
        validarNome(novoNome)?.let { return it.toRenomear() }
        conflitoDeNome(novoNome, excetoId = id)?.let {
            return ResultadoRenomearJogador.NomeDuplicado(it)
        }

        val atualizado = atual.copy(nome = prepararNome(novoNome))
        return ResultadoRenomearJogador.Sucesso(
            cadastro = copy(jogadores = jogadores + (id to atualizado)),
            jogador = atualizado,
        )
    }

    fun arquivar(id: JogadorId): ResultadoArquivarJogador {
        val atual = jogadores[id] ?: return ResultadoArquivarJogador.JogadorNaoEncontrado
        val atualizado = atual.copy(arquivado = true)
        return ResultadoArquivarJogador.Sucesso(
            cadastro = copy(jogadores = jogadores + (id to atualizado)),
            jogador = atualizado,
        )
    }

    fun restaurar(id: JogadorId): ResultadoRestaurarJogador {
        val atual = jogadores[id] ?: return ResultadoRestaurarJogador.JogadorNaoEncontrado
        val atualizado = atual.copy(arquivado = false)
        return ResultadoRestaurarJogador.Sucesso(
            cadastro = copy(jogadores = jogadores + (id to atualizado)),
            jogador = atualizado,
        )
    }

    fun tornarFixo(id: JogadorId): ResultadoTornarFixoJogador {
        val atual = jogadores[id] ?: return ResultadoTornarFixoJogador.JogadorNaoEncontrado
        val atualizado = atual.copy(convidado = false)
        return ResultadoTornarFixoJogador.Sucesso(
            cadastro = copy(jogadores = jogadores + (id to atualizado)),
            jogador = atualizado,
        )
    }

    private fun conflitoDeNome(nome: String, excetoId: JogadorId? = null): Jogador? {
        val chave = normalizarNome(nome)
        return jogadores.values.firstOrNull {
            it.nomeNormalizado == chave && it.id != excetoId
        }
    }

    private sealed class ErroNome {
        data object Vazio : ErroNome()
        data object MuitoLongo : ErroNome()

        fun toAdicionar(): ResultadoAdicionarJogador = when (this) {
            Vazio -> ResultadoAdicionarJogador.NomeVazio
            MuitoLongo -> ResultadoAdicionarJogador.NomeMuitoLongo
        }

        fun toRenomear(): ResultadoRenomearJogador = when (this) {
            Vazio -> ResultadoRenomearJogador.NomeVazio
            MuitoLongo -> ResultadoRenomearJogador.NomeMuitoLongo
        }
    }

    private fun validarNome(nome: String): ErroNome? = when {
        nomeVazio(nome) -> ErroNome.Vazio
        nomeMuitoLongo(nome) -> ErroNome.MuitoLongo
        else -> null
    }
}

sealed class ResultadoAdicionarJogador {
    data class Sucesso(
        val cadastro: CadastroDeJogadores,
        val jogador: Jogador,
    ) : ResultadoAdicionarJogador()

    data object NomeVazio : ResultadoAdicionarJogador()
    data object NomeMuitoLongo : ResultadoAdicionarJogador()
    data class NomeDuplicado(val jogadorExistente: Jogador) : ResultadoAdicionarJogador()
}

sealed class ResultadoRenomearJogador {
    data class Sucesso(
        val cadastro: CadastroDeJogadores,
        val jogador: Jogador,
    ) : ResultadoRenomearJogador()

    data object NomeVazio : ResultadoRenomearJogador()
    data object NomeMuitoLongo : ResultadoRenomearJogador()
    data class NomeDuplicado(val jogadorExistente: Jogador) : ResultadoRenomearJogador()
    data object JogadorNaoEncontrado : ResultadoRenomearJogador()
}

sealed class ResultadoArquivarJogador {
    data class Sucesso(
        val cadastro: CadastroDeJogadores,
        val jogador: Jogador,
    ) : ResultadoArquivarJogador()

    data object JogadorNaoEncontrado : ResultadoArquivarJogador()
}

sealed class ResultadoRestaurarJogador {
    data class Sucesso(
        val cadastro: CadastroDeJogadores,
        val jogador: Jogador,
    ) : ResultadoRestaurarJogador()

    data object JogadorNaoEncontrado : ResultadoRestaurarJogador()
}

sealed class ResultadoTornarFixoJogador {
    data class Sucesso(
        val cadastro: CadastroDeJogadores,
        val jogador: Jogador,
    ) : ResultadoTornarFixoJogador()

    data object JogadorNaoEncontrado : ResultadoTornarFixoJogador()
}
