package com.aicolorpredict.analytics.data

import com.aicolorpredict.analytics.data.importer.CsvExporter
import com.aicolorpredict.analytics.data.importer.CsvImporter
import com.aicolorpredict.analytics.data.importer.JsonExporter
import com.aicolorpredict.analytics.data.importer.JsonImporter
import com.aicolorpredict.analytics.domain.model.Round
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ImportExportTest {

    private val sampleRounds = listOf(
        Round.fromNumber(id = 1, epochMs = 1700000000000L, number = 4, prior = listOf(1, 2, 3)),
        Round.fromNumber(id = 2, epochMs = 1700000001000L, number = 7, prior = listOf(1, 2, 3, 4)),
        Round.fromNumber(id = 3, epochMs = 1700000002000L, number = 0, prior = listOf(1, 2, 3, 4, 7))
    )

    @Test fun `CSV round-trips losslessly`() {
        val baos = ByteArrayOutputStream()
        CsvExporter.write(sampleRounds, baos)
        val bytes = baos.toByteArray()
        val parsed = CsvImporter.parse(ByteArrayInputStream(bytes))
        assertThat(parsed).hasSize(sampleRounds.size)
        assertThat(parsed.map { it.number }).isEqualTo(sampleRounds.map { it.number })
    }

    @Test fun `JSON round-trips losslessly`() {
        val baos = ByteArrayOutputStream()
        JsonExporter.write(sampleRounds, baos)
        val bytes = baos.toByteArray()
        val parsed = JsonImporter.parse(ByteArrayInputStream(bytes))
        assertThat(parsed).hasSize(sampleRounds.size)
        assertThat(parsed.map { it.number }).isEqualTo(sampleRounds.map { it.number })
    }

    @Test fun `CSV importer accepts narrow format`() {
        val csv = """
            roundId,time,number
            1,1700000000000,4
            2,1700000001000,7
            3,1700000002000,0
        """.trimIndent()
        val parsed = CsvImporter.parse(ByteArrayInputStream(csv.toByteArray()))
        assertThat(parsed).hasSize(3)
        assertThat(parsed.map { it.number }).isEqualTo(listOf(4, 7, 0))
    }
}
