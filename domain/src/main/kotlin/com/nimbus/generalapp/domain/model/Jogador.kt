package com.nimbus.generalapp.domain.model

import java.text.Normalizer
import java.util.Locale

data class Jogador(
    val id: JogadorId,
    /** Nome para exibir: espaços das pontas cortados e espaços duplicados reduzidos a um. */
    val nome: String,
    /** Cor ARGB. */
    val cor: Int,
    val arquivado: Boolean = false,
    val convidado: Boolean = false,
) {
    val nomeNormalizado: String = normalizarNome(nome)
}

/**
 * Corta espaços das pontas, reduz espaços duplicados a um, minúsculas (Locale.ROOT)
 * e remove acentos/diacríticos (NFD + remoção de marcas).
 */
fun normalizarNome(nome: String): String {
    val preparado = prepararNome(nome)
    val nfd = Normalizer.normalize(preparado, Normalizer.Form.NFD)
    return nfd.replace("\\p{Mn}+".toRegex(), "").lowercase(Locale.ROOT)
}

/** Espaços das pontas cortados e espaços duplicados reduzidos a um. */
fun prepararNome(nome: String): String =
    nome.trim().replace("\\s+".toRegex(), " ")

fun nomeVazio(nome: String): Boolean =
    prepararNome(nome).isEmpty()

fun nomeMuitoLongo(nome: String): Boolean =
    prepararNome(nome).length > TAMANHO_MAXIMO_NOME

const val TAMANHO_MAXIMO_NOME = 20
