package no.nav.helse.sykdomstidslinje.dag

import no.nav.helse.hendelse.Sykdomshendelse
import no.nav.helse.sykdomstidslinje.SykdomstidslinjeVisitor
import java.time.LocalDate

internal class Nulldag internal constructor(gjelder: LocalDate, hendelse: Sykdomshendelse): Dag(gjelder, hendelse){
    override fun accept(visitor: SykdomstidslinjeVisitor) {}

    override fun antallSykedagerHvorViTellerMedHelg() = 0

    override fun antallSykedagerHvorViIkkeTellerMedHelg(): Int = 0

    override fun tilDag() =  InferertArbeidsdag(dagen,hendelse)//ikkeSykedag(dagen, hendelse)

    override fun toString() = formatter.format(dagen) + "\tNulldag"

    override fun dagType() = JsonDagType.NULLDAG
}
