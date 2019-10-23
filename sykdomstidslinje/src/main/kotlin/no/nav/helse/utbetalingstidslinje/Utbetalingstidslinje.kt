package no.nav.helse.utbetalingstidslinje

import no.nav.helse.sykdomstidslinje.SykdomstidslinjeVisitor
import no.nav.helse.sykdomstidslinje.Syketilfelle
import no.nav.helse.sykdomstidslinje.dag.Dag
import no.nav.helse.sykdomstidslinje.dag.Sykedag
import java.math.BigDecimal

data class Utbetalingsdag(
    val dag: Dag
)

class Utbetalingstidslinje(
    private val syketilfelle: Syketilfelle,
    private val dagsats: BigDecimal
) : SykdomstidslinjeVisitor {
    val utbetalingsdager = mutableListOf<Utbetalingsdag>()
    init {
        syketilfelle.tidslinje.flatten()
            .dropWhile { it.dagen != syketilfelle.førsteUtbetalingsdag }
            .forEach { it.accept(this) }
    }

    override fun visitSykedag(sykedag: Sykedag) {
        utbetalingsdager.add(Utbetalingsdag(sykedag))
    }
}
