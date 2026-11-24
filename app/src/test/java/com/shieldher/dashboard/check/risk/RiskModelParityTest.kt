package com.shieldher.dashboard.check.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import kotlin.math.abs

class RiskModelParityTest {

    private fun loadModel(): RiskModel {
        val binFile = File("src/main/assets/account_risk_model_v1.bin").let {
            if (it.exists()) it else File("app/src/main/assets/account_risk_model_v1.bin")
        }
        assertTrue("Model asset file must exist at ${binFile.absolutePath}", binFile.exists())
        return FileInputStream(binFile).use { RiskModel.load(it) }
    }

    private data class GoldenRow(
        val id: String,
        val category: String,
        val followers: Long?,
        val following: Long?,
        val descriptionLength: Int,
        val externalUrl: Boolean,
        val pRaw: Double?,
        val pCal: Double?,
        val state: String,
        val label: String
    )

    private fun loadGoldenRows(): List<GoldenRow> {
        val csvStream = javaClass.classLoader?.getResourceAsStream("account_risk_golden_v1.csv")
            ?: File("src/test/resources/account_risk_golden_v1.csv").let {
                if (it.exists()) FileInputStream(it)
                else FileInputStream(File("app/src/test/resources/account_risk_golden_v1.csv"))
            }

        val lines = csvStream.bufferedReader().readLines()
        val header = lines.first().split(",")
        val idIdx = header.indexOf("id")
        val catIdx = header.indexOf("category")
        val fwIdx = header.indexOf("followers")
        val fgIdx = header.indexOf("following")
        val descIdx = header.indexOf("description_length")
        val urlIdx = header.indexOf("external_url")
        val pRawIdx = header.indexOf("p_raw")
        val pCalIdx = header.indexOf("p_cal")
        val stateIdx = header.indexOf("state")
        val labelIdx = header.indexOf("label")

        val rows = mutableListOf<GoldenRow>()
        for (line in lines.drop(1)) {
            if (line.isBlank()) continue
            val parts = line.split(",")
            val id = parts[idIdx]
            val cat = parts[catIdx]
            val fw = parts[fwIdx].ifBlank { null }?.toLongOrNull()
            val fg = parts[fgIdx].ifBlank { null }?.toLongOrNull()
            val desc = parts[descIdx].toInt()
            val url = parts[urlIdx].toInt() == 1
            val pRaw = parts[pRawIdx].ifBlank { null }?.toDoubleOrNull()
            val pCal = parts[pCalIdx].ifBlank { null }?.toDoubleOrNull()
            val state = parts[stateIdx]
            val label = parts[labelIdx]

            rows.add(GoldenRow(id, cat, fw, fg, desc, url, pRaw, pCal, state, label))
        }
        return rows
    }

    @Test
    fun parityCheckAllGoldenRows() {
        val model = loadModel()
        val evaluator = RiskEvaluator(model)
        val goldenRows = loadGoldenRows()

        assertTrue("Golden rows must contain at least 700 rows", goldenRows.size >= 700)

        var maxDiffRaw = 0.0
        var maxDiffCal = 0.0
        val catCount = mutableMapOf<String, Int>()
        val mismatches = mutableListOf<String>()

        for (row in goldenRows) {
            catCount[row.category] = (catCount[row.category] ?: 0) + 1

            val feats = RiskFeatures.transform(
                followers = row.followers,
                following = row.following,
                descriptionLength = row.descriptionLength,
                hasLink = row.externalUrl
            )

            if (feats == null) {
                // Must match unsupported_input
                if (row.state != "unsupported_input") {
                    mismatches.add(
                        "Row ${row.id} (${row.category}): expected state '${row.state}' but features were null (unsupported)"
                    )
                }
                continue
            }

            val score = evaluator.evaluate(feats)

            if (row.pRaw != null) {
                val diffRaw = abs(score.pRaw!! - row.pRaw)
                if (diffRaw > maxDiffRaw) maxDiffRaw = diffRaw
                if (diffRaw > 1e-9) {
                    mismatches.add(
                        "Row ${row.id} (${row.category}): pRaw diff $diffRaw > 1e-9 (eval=${score.pRaw}, exp=${row.pRaw})"
                    )
                }
            }

            if (row.pCal != null) {
                val diffCal = abs(score.pCal!! - row.pCal)
                if (diffCal > maxDiffCal) maxDiffCal = diffCal
                if (diffCal > 1e-9) {
                    mismatches.add(
                        "Row ${row.id} (${row.category}): pCal diff $diffCal > 1e-9 (eval=${score.pCal}, exp=${row.pCal})"
                    )
                }
            }

            if (score.state != row.state) {
                mismatches.add(
                    "Row ${row.id} (${row.category}): state mismatch (eval='${score.state}', exp='${row.state}')"
                )
            }

            if (score.label != row.label) {
                mismatches.add(
                    "Row ${row.id} (${row.category}): label mismatch (eval='${score.label}', exp='${row.label}')"
                )
            }
        }

        if (mismatches.isNotEmpty()) {
            val preview = mismatches.take(10).joinToString("\n")
            fail("Parity check failed with ${mismatches.size} mismatching rows:\n$preview")
        }

        // Print stats in assertion / message form
        assertTrue("Max raw diff ($maxDiffRaw) <= 1e-9", maxDiffRaw <= 1e-9)
        assertTrue("Max cal diff ($maxDiffCal) <= 1e-9", maxDiffCal <= 1e-9)
        assertTrue("Category counts verified: $catCount", catCount.size >= 4)
    }

    @Test
    fun evaluatorIsDeterministicAndReloadPreservesScores() {
        val model1 = loadModel()
        val model2 = loadModel()
        val eval1 = RiskEvaluator(model1)
        val eval2 = RiskEvaluator(model2)

        val feats = RiskFeatures.transform(500L, 200L, 40, false)!!

        val score1A = eval1.evaluate(feats)
        val score1B = eval1.evaluate(feats)
        val score2 = eval2.evaluate(feats)

        assertEquals("Same evaluator must be deterministic (pRaw)", score1A.pRaw, score1B.pRaw)
        assertEquals("Same evaluator must be deterministic (pCal)", score1A.pCal, score1B.pCal)
        assertEquals("Same evaluator must be deterministic (state)", score1A.state, score1B.state)
        assertEquals("Same evaluator must be deterministic (label)", score1A.label, score1B.label)

        assertEquals("Reloaded model evaluator must produce identical pRaw", score1A.pRaw, score2.pRaw)
        assertEquals("Reloaded model evaluator must produce identical pCal", score1A.pCal, score2.pCal)
        assertEquals("Reloaded model evaluator must produce identical state", score1A.state, score2.state)
        assertEquals("Reloaded model evaluator must produce identical label", score1A.label, score2.label)
    }
}
