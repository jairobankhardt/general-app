package com.nimbus.generalapp.domain.model

enum class Casa {
    UM,
    DOIS,
    TRES,
    QUATRO,
    CINCO,
    SEIS,
    FULL,
    SEGUIDA,
    QUADRA,
    GENERAL;

    val ehNumerica: Boolean
        get() = numero != null

    /** Valor do dado correspondente (1..6), ou null para casas especiais. */
    val numero: Int?
        get() = when (this) {
            UM -> 1
            DOIS -> 2
            TRES -> 3
            QUATRO -> 4
            CINCO -> 5
            SEIS -> 6
            FULL, SEGUIDA, QUADRA, GENERAL -> null
        }

    companion object {
        fun numerica(numero: Int): Casa {
            require(numero in 1..6) { "Número de casa inválido: $numero" }
            return entries.first { it.numero == numero }
        }
    }
}
