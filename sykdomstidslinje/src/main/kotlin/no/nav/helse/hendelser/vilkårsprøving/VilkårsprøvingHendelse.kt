package no.nav.helse.hendelser.vilkårsprøving

import no.nav.helse.hendelse.SakskompleksHendelse
import no.nav.helse.person.ArbeidstakerHendelse

class VilkårsprøvingHendelse(private val sakskompleksID: String): ArbeidstakerHendelse, SakskompleksHendelse {

    override fun organisasjonsnummer(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sakskompleksId(): String {
        return sakskompleksID
    }

    override fun aktørId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}