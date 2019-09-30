package no.nav.helse.sykdomstidslinje

import java.time.LocalDateTime

internal class Testhendelse(private val rapportertdato: LocalDateTime = LocalDateTime.of(2019, 9, 16, 10, 45)) :
    Sykdomshendelse {
    override fun rapportertdato(): LocalDateTime {
        return rapportertdato
    }

    override fun compareTo(other: Sykdomshendelse): Int {
        return this.rapportertdato().compareTo(other.rapportertdato())
    }
}
