package com.aicolorpredict.analytics.data.importer

import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.util.DateUtils
import java.io.OutputStream

/**
 * CSV exporter — emits the wide format (RoundID, Time, Number, Color, Result,
 * PreviousRound, Previous3, ..., Previous1000, Streak, Odd, Even, Small, Big,
 * Green, Red, Violet) so the export round-trips losslessly with the importer.
 */
object CsvExporter {

    private val HEADER = listOf(
        "RoundID", "Time", "Number", "Color", "Result",
        "PreviousRound", "Previous3", "Previous5", "Previous10", "Previous20",
        "Previous50", "Previous100", "Previous500", "Previous1000",
        "CurrentStreak", "Odd", "Even", "Small", "Big", "Green", "Red", "Violet"
    )

    fun write(rounds: List<Round>, out: OutputStream) {
        out.bufferedWriter().use { w ->
            w.appendLine(HEADER.joinToString(","))
            for (r in rounds) {
                val colorStr = r.colors.joinToString("|") { it.display }
                val row = listOf(
                    r.id.toString(),
                    DateUtils.toIsoUtc(r.epochMs),
                    r.number.toString(),
                    "\"$colorStr\"",
                    r.number.toString(),
                    r.previousNumber?.toString().orEmpty(),
                    r.previous3.joinToString("|"),
                    r.previous5.joinToString("|"),
                    r.previous10.joinToString("|"),
                    r.previous20.joinToString("|"),
                    r.previous50.joinToString("|"),
                    r.previous100.joinToString("|"),
                    r.previous500.joinToString("|"),
                    r.previous1000.joinToString("|"),
                    r.streak.toString(),
                    if (r.isOdd) "1" else "0",
                    if (r.isEven) "1" else "0",
                    if (r.isSmall) "1" else "0",
                    if (r.isBig) "1" else "0",
                    if (r.isGreen) "1" else "0",
                    if (r.isRed) "1" else "0",
                    if (r.isViolet) "1" else "0"
                )
                w.appendLine(row.joinToString(","))
            }
        }
    }
}
