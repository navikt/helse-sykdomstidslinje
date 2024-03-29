package no.nav.helse.hendelser.saksbehandling

import no.nav.helse.behov.Behov
import no.nav.helse.hendelse.SakskompleksHendelse
import no.nav.helse.person.ArbeidstakerHendelse

class ManuellSaksbehandlingHendelse(private val manuellSaksbehandling: Behov) : ArbeidstakerHendelse, SakskompleksHendelse {

    fun saksbehandler() =
            manuellSaksbehandling.get<String>("saksbehandlerIdent")!!

    fun utbetalingGodkjent(): Boolean =
            (manuellSaksbehandling.løsning() as Map<String, Boolean>?)?.getValue("godkjent") as Boolean? ?: false

    override fun sakskompleksId() =
            manuellSaksbehandling.get<String>("sakskompleksId")!!

    override fun aktørId() =
            manuellSaksbehandling.get<String>("aktørId")!!

    override fun organisasjonsnummer() =
            manuellSaksbehandling.get<String>("organisasjonsnummer")!!
}
