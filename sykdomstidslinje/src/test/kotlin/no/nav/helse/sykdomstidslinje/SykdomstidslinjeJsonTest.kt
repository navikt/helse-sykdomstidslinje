package no.nav.helse.sykdomstidslinje

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.sykdomstidslinje.Sykdomstidslinje.Companion.egenmeldingsdag
import no.nav.helse.sykdomstidslinje.Sykdomstidslinje.Companion.ferie
import no.nav.helse.sykdomstidslinje.Sykdomstidslinje.Companion.ikkeSykedag
import no.nav.helse.sykdomstidslinje.Sykdomstidslinje.Companion.permisjonsdag
import no.nav.helse.sykdomstidslinje.Sykdomstidslinje.Companion.studiedag
import no.nav.helse.sykdomstidslinje.Sykdomstidslinje.Companion.sykedag
import no.nav.helse.sykdomstidslinje.Sykdomstidslinje.Companion.utenlandsdag
import no.nav.helse.sykdomstidslinje.dag.Dag
import no.nav.helse.sykdomstidslinje.dag.JsonDagType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class SykdomstidslinjeJsonTest {
    private companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    private val inntektsmeldingHendelse =
        InntektsmeldingHendelse()
    private val sendtSøknadHendelse =
        SendtSøknadHendelse()


    @Test
    fun `gitt en tidslinje så serialiseres den med en json pr hendelse, som refereses til med id fra dag`() {

        val tidslinje = Sykdomstidslinje.sykedager(
            LocalDate.of(2019, 10, 7),
            LocalDate.of(2019, 10, 10), sendtSøknadHendelse
        )

        val tidslinjeJson = objectMapper.readTree(tidslinje.toJson())
        tidslinjeJson["hendelser"].elements().forEach {
            assertEquals(sendtSøknadHendelse.hendelseId(), it["hendelseId"].asText())
        }
        tidslinjeJson["dager"].elements().forEach {
            assertEquals(sendtSøknadHendelse.hendelseId(), it["hendelseId"].asText())
        }
    }

    @Test
    fun `hendeler på erstattede dager blir også normalisert`() {

        val tidslinjeB = Sykdomstidslinje.ikkeSykedager(
            LocalDate.of(2019, 10, 7),
            LocalDate.of(2019, 10, 10), inntektsmeldingHendelse
        )
        val tidslinjeC = Sykdomstidslinje.sykedager(
            LocalDate.of(2019, 10, 7),
            LocalDate.of(2019, 10, 10), sendtSøknadHendelse
        )

        val combined = tidslinjeB + tidslinjeC

        val tidslinjeJson = objectMapper.readTree(combined.toJson()).also { println(it) }


        val hendelser = tidslinjeJson["hendelser"]
        assertEquals(hendelser.size(), 2)
        val hendelseMap = hendelser.groupBy { it["hendelseId"].asText() }
        tidslinjeJson["dager"].elements().forEach {
            val hendelseId = it["hendelseId"].asText()
            assertTrue(hendelseMap.containsKey(hendelseId))
            if (!it["erstatter"].isEmpty)
                it["erstatter"].onEach {
                    assertTrue(hendelseMap.containsKey(it["hendelseId"].asText()))
                }
        }
    }

    @Test
    fun `lagring og restoring av en sykdomstidslinje med har de samme egenskapene som den opprinnelige`() {
        val tidslinjeA = Sykdomstidslinje.ikkeSykedager(
            LocalDate.of(2019, 10, 1),
            LocalDate.of(2019, 10, 3), inntektsmeldingHendelse
        )
        val tidslinjeB = Sykdomstidslinje.ikkeSykedager(
            LocalDate.of(2019, 10, 7),
            LocalDate.of(2019, 10, 10), inntektsmeldingHendelse
        )
        val tidslinjeC = Sykdomstidslinje.sykedager(
            LocalDate.of(2019, 10, 7),
            LocalDate.of(2019, 10, 10), sendtSøknadHendelse
        )

        val combined = tidslinjeA + tidslinjeB + tidslinjeC
        val json = combined.toJson()

        val restored = Sykdomstidslinje.fromJson(json,
            TestHendelseDeserializer()
        )

        assertSykdomstidslinjerEquals(combined, restored)
    }

    @Test
    fun `lagring og restoring av en sykdomstidslinje med søknader og inntektsmeldinger har like egenskaper`() {
        val egenmelding =
            Sykdomstidslinje.egenmeldingsdager(
                LocalDate.of(2019, 9, 30),
                LocalDate.of(2019, 10, 1),
                sendtSøknadHendelse
            )
        val sykedagerA = Sykdomstidslinje.sykedager(
            LocalDate.of(2019, 10, 2),
            LocalDate.of(2019, 10, 4), sendtSøknadHendelse
        )
        val ikkeSykedager = Sykdomstidslinje.ikkeSykedager(
            LocalDate.of(2019, 10, 7),
            LocalDate.of(2019, 10, 10), inntektsmeldingHendelse
        )
        val sykedagerB = Sykdomstidslinje.sykedager(
            LocalDate.of(2019, 10, 7),
            LocalDate.of(2019, 10, 10), sendtSøknadHendelse
        )

        val combined = egenmelding + sykedagerA + ikkeSykedager + sykedagerB
        val json = combined.toJson()

        val restored = Sykdomstidslinje.fromJson(json,
            TestHendelseDeserializer()
        )
        assertSykdomstidslinjerEquals(combined, restored)
    }

    @Test
    fun `sykdomstidslinje med alle typer dager blir serialisert riktig`() {
        egenmeldingsdag(LocalDate.of(2019, 10, 7), inntektsmeldingHendelse).also {
            assertJsonDagType(JsonDagType.EGENMELDINGSDAG, it)
        }

        sykedag(LocalDate.of(2019, 10, 8), sendtSøknadHendelse).also {
            assertJsonDagType(JsonDagType.SYKEDAG, it)
        }

        ferie(LocalDate.of(2019, 10, 9), sendtSøknadHendelse).also {
            assertJsonDagType(JsonDagType.FERIEDAG, it)
        }

        permisjonsdag(LocalDate.of(2019, 10, 11), sendtSøknadHendelse).also {
            assertJsonDagType(JsonDagType.PERMISJONSDAG, it)
        }

        studiedag(LocalDate.of(2019, 10, 17), sendtSøknadHendelse).also {
            assertJsonDagType(JsonDagType.STUDIEDAG, it)
        }

        ikkeSykedag(LocalDate.of(2019, 10, 18), sendtSøknadHendelse).also {
            assertJsonDagType(JsonDagType.ARBEIDSDAG, it)
        }

        utenlandsdag(LocalDate.of(2019, 10, 22), sendtSøknadHendelse).also {
            assertJsonDagType(JsonDagType.UTENLANDSDAG, it)
        }
    }

    private fun assertJsonDagType(expectedType: JsonDagType, dag: Dag) {
        assertTrue(dag.toJson().contains("\"${expectedType.name}\""), "Tidslinje inneholder ikke dag-type ${expectedType.name}")
    }

    private fun assertSykdomstidslinjerEquals(expected: Sykdomstidslinje, actual: Sykdomstidslinje) {
        assertEquals(expected.startdato(), actual.startdato())
        assertEquals(expected.sluttdato(), actual.sluttdato())
        assertEquals(expected.antallSykedagerHvorViIkkeTellerMedHelg(), actual.antallSykedagerHvorViIkkeTellerMedHelg())
        assertEquals(expected.antallSykedagerHvorViTellerMedHelg(), actual.antallSykedagerHvorViTellerMedHelg())
        assertEquals(expected.length(), actual.length())

        val actualDager = actual.flatten()
        expected.flatten().forEachIndexed { key, dag ->
            assertDagEquals(dag, actualDager[key])
        }
    }

    private fun assertDagEquals(expected: Dag, actual: Dag) {
        assertEquals(expected::class, actual::class)
        assertEquals(expected.startdato(), actual.startdato())
        assertEquals(expected.sluttdato(), actual.sluttdato())
        assertEquals(expected.dagerErstattet().size, actual.dagerErstattet().size)

        val actualDager = actual.dagerErstattet()
        expected.dagerErstattet().forEachIndexed { key, dag ->
            assertDagEquals(dag, actualDager[key])
        }
    }

    private class TestHendelseDeserializer : SykdomstidslinjeHendelse.Deserializer {
        override fun deserialize(jsonNode: JsonNode): SykdomstidslinjeHendelse {
            return when (jsonNode["type"].textValue()) {
                HendelseType.Inntektsmelding.name -> InntektsmeldingHendelse(
                    jsonNode["hendelseId"].asText()
                )
                HendelseType.SendtSøknad.name -> InntektsmeldingHendelse(
                    jsonNode["hendelseId"].asText()
                )
                else -> throw RuntimeException("ukjent type")
            }
        }

    }

    private enum class HendelseType {
        Inntektsmelding,
        SendtSøknad
    }

    private class InntektsmeldingHendelse(hendelseId: String = UUID.randomUUID().toString()) :
        SykdomstidslinjeHendelse(hendelseId) {

        override fun rapportertdato(): LocalDateTime {
            return LocalDateTime.now()
        }

        override fun sykdomstidslinje(): Sykdomstidslinje {
            TODO("not implemented")
        }

        override fun nøkkelHendelseType() = Dag.NøkkelHendelseType.Inntektsmelding

        override fun toJson(): JsonNode {
            return (super.toJson() as ObjectNode).put("type", HendelseType.Inntektsmelding.name)
        }
    }

    private class SendtSøknadHendelse(hendelseId: String = UUID.randomUUID().toString()) :
        SykdomstidslinjeHendelse(hendelseId) {

        override fun rapportertdato(): LocalDateTime {
            return LocalDateTime.now()
        }

        override fun sykdomstidslinje(): Sykdomstidslinje {
            TODO("not implemented")
        }

        override fun nøkkelHendelseType() = Dag.NøkkelHendelseType.Søknad

        override fun toJson(): JsonNode {
            return (super.toJson() as ObjectNode).put("type", HendelseType.SendtSøknad.name)
        }
    }
}
