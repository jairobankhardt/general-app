package com.nimbus.generalapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CadastroDeJogadoresTest {

    private val cor = 0xFF112233.toInt()

    @Test
    fun joaoJoaoEJoaoComEspacosConflitamEntreSi() {
        assertEquals("joao", normalizarNome("João"))
        assertEquals("joao", normalizarNome("joão"))
        assertEquals("joao", normalizarNome(" JOAO "))

        val aposPrimeiro = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "João")
        val conflito = aposPrimeiro.adicionar(JogadorId("2"), "joão", cor)
        assertTrue(conflito is ResultadoAdicionarJogador.NomeDuplicado)

        val conflitoEspacos = aposPrimeiro.adicionar(JogadorId("3"), " JOAO ", cor)
        assertTrue(conflitoEspacos is ResultadoAdicionarJogador.NomeDuplicado)
    }

    @Test
    fun joseEJoseSemAcentoConflitam() {
        assertEquals("jose", normalizarNome("José"))
        assertEquals("jose", normalizarNome("jose"))

        val aposJose = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "José")
        val conflito = aposJose.adicionar(JogadorId("2"), "jose", cor)
        assertTrue(conflito is ResultadoAdicionarJogador.NomeDuplicado)
    }

    @Test
    fun espacosInternosDuplicadosSaoColapsadosNaNormalizacao() {
        assertEquals("ana maria", normalizarNome("Ana  Maria"))
        assertEquals("Ana Maria", prepararNome("Ana  Maria"))
    }

    @Test
    fun nomeVazioOuSoComEspacosEInvalido() {
        val cadastro = CadastroDeJogadores()
        assertTrue(cadastro.adicionar(JogadorId("1"), "", cor) is ResultadoAdicionarJogador.NomeVazio)
        assertTrue(cadastro.adicionar(JogadorId("2"), "   ", cor) is ResultadoAdicionarJogador.NomeVazio)
    }

    @Test
    fun nomeComMaisDe20CaracteresEInvalidoEExatamente20EPermitido() {
        val cadastro = CadastroDeJogadores()
        val exatamente20 = "a".repeat(20)
        val com21 = "a".repeat(21)

        val ok = cadastro.adicionar(JogadorId("1"), exatamente20, cor)
        assertTrue(ok is ResultadoAdicionarJogador.Sucesso)

        val longo = cadastro.adicionar(JogadorId("2"), com21, cor)
        assertTrue(longo is ResultadoAdicionarJogador.NomeMuitoLongo)

        val comEspacosQueColapsamPara21 = "a".repeat(19) + "  b"
        assertEquals(21, prepararNome(comEspacosQueColapsamPara21).length)
        assertTrue(
            cadastro.adicionar(JogadorId("3"), comEspacosQueColapsamPara21, cor)
                is ResultadoAdicionarJogador.NomeMuitoLongo,
        )
    }

    @Test
    fun conflitoComJogadorArquivadoIndicaQueEstaArquivado() {
        var cadastro = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "Maria")
        cadastro = sucessoArquivar(cadastro, JogadorId("1"))

        val resultado = cadastro.adicionar(JogadorId("2"), "maria", cor)
        val duplicado = resultado as ResultadoAdicionarJogador.NomeDuplicado
        assertTrue(duplicado.jogadorExistente.arquivado)
        assertEquals(JogadorId("1"), duplicado.jogadorExistente.id)
    }

    @Test
    fun renomearAnaParaAnaMesmoJogadorEPermitido() {
        var cadastro = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "ana")
        val resultado = cadastro.renomear(JogadorId("1"), "Ana")
        val sucesso = resultado as ResultadoRenomearJogador.Sucesso
        assertEquals("Ana", sucesso.jogador.nome)
        assertEquals("ana", sucesso.jogador.nomeNormalizado)
    }

    @Test
    fun renomearParaNomeDeOutroJogadorEBloqueado() {
        var cadastro = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "Ana")
        cadastro = sucessoAdicionar(cadastro, JogadorId("2"), "Bob")

        val resultado = cadastro.renomear(JogadorId("2"), "ana")
        assertTrue(resultado is ResultadoRenomearJogador.NomeDuplicado)
    }

    @Test
    fun convidadoTambemObedeceAUnicidadeDeNome() {
        val aposFixo = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "Carla")
        val convidado = aposFixo.adicionar(JogadorId("2"), "carla", cor, convidado = true)
        assertTrue(convidado is ResultadoAdicionarJogador.NomeDuplicado)

        val aposConvidado = sucessoAdicionar(
            CadastroDeJogadores(),
            JogadorId("3"),
            "Diego",
            convidado = true,
        )
        val outroConvidado = aposConvidado.adicionar(
            JogadorId("4"),
            "diego",
            cor,
            convidado = true,
        )
        assertTrue(outroConvidado is ResultadoAdicionarJogador.NomeDuplicado)
    }

    @Test
    fun tornarFixoRemoveMarcaDeConvidado() {
        var cadastro = sucessoAdicionar(
            CadastroDeJogadores(),
            JogadorId("1"),
            "Eva",
            convidado = true,
        )
        assertTrue(cadastro.porId(JogadorId("1"))!!.convidado)

        val resultado = cadastro.tornarFixo(JogadorId("1")) as ResultadoTornarFixoJogador.Sucesso
        assertFalse(resultado.jogador.convidado)
        assertFalse(resultado.cadastro.porId(JogadorId("1"))!!.convidado)
    }

    @Test
    fun arquivarERestaurarAlteramFlagSemApagarJogador() {
        var cadastro = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "Fábio")
        cadastro = sucessoArquivar(cadastro, JogadorId("1"))
        assertTrue(cadastro.porId(JogadorId("1"))!!.arquivado)

        val restaurado = cadastro.restaurar(JogadorId("1")) as ResultadoRestaurarJogador.Sucesso
        assertFalse(restaurado.jogador.arquivado)
        assertNotNull(restaurado.cadastro.porId(JogadorId("1")))
    }

    @Test
    fun ativosNaoListaArquivados() {
        var cadastro = sucessoAdicionar(CadastroDeJogadores(), JogadorId("1"), "Ana")
        cadastro = sucessoAdicionar(cadastro, JogadorId("2"), "Bob")
        cadastro = sucessoArquivar(cadastro, JogadorId("1"))

        val ativos = cadastro.ativos()
        assertEquals(1, ativos.size)
        assertEquals(JogadorId("2"), ativos.single().id)
    }

    @Test
    fun aposRenomearConsultaPorIdDevolveNovoNomePartidasReferenciamSoOId() {
        var cadastro = sucessoAdicionar(CadastroDeJogadores(), JogadorId("partida-ref"), "Gabi")
        val id = JogadorId("partida-ref")

        cadastro = (cadastro.renomear(id, "Gabrielle") as ResultadoRenomearJogador.Sucesso).cadastro

        assertEquals("Gabrielle", cadastro.porId(id)!!.nome)
        assertEquals("gabrielle", cadastro.porId(id)!!.nomeNormalizado)
        assertNull(cadastro.porNomeNormalizado("Gabi"))
        assertEquals(id, cadastro.porNomeNormalizado("Gabrielle")!!.id)
    }

    private fun sucessoAdicionar(
        cadastro: CadastroDeJogadores,
        id: JogadorId,
        nome: String,
        convidado: Boolean = false,
    ): CadastroDeJogadores {
        val resultado = cadastro.adicionar(id, nome, cor, convidado)
        return (resultado as ResultadoAdicionarJogador.Sucesso).cadastro
    }

    private fun sucessoArquivar(
        cadastro: CadastroDeJogadores,
        id: JogadorId,
    ): CadastroDeJogadores {
        return (cadastro.arquivar(id) as ResultadoArquivarJogador.Sucesso).cadastro
    }
}
