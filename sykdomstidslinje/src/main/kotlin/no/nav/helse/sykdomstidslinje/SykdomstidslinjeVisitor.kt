package no.nav.helse.sykdomstidslinje

interface SykdomstidslinjeVisitor {
    fun visitArbeidsdag(arbeidsdag: Arbeidsdag){}
    fun visitFeriedag(feriedag: Feriedag){}
    fun visitHelgedag(helgedag: Helgedag){}
    fun visitSykedag(sykedag: Sykedag){}
    fun visitSykHelgedag(sykHelgedag: SykHelgedag){}
    fun preVisitComposite(compositeSykdomstidslinje: CompositeSykdomstidslinje){}
    fun postVisitComposite(compositeSykdomstidslinje: CompositeSykdomstidslinje){}
}