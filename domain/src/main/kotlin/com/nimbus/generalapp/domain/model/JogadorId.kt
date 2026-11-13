package com.nimbus.generalapp.domain.model

/**
 * Identificador opaco de jogador. O valor (ex.: UUID) é gerado fora do domínio.
 */
@JvmInline
value class JogadorId(val valor: String)
