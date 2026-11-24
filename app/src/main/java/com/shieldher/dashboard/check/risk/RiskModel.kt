package com.shieldher.dashboard.check.risk

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * Model score and state output produced by [RiskEvaluator].
 * Internal to the check package; probabilities are never exposed to UI.
 */
internal data class RiskScore(
    val pRaw: Double?,
    val pCal: Double?,
    val state: String,
    val label: String
)

/**
 * In-memory representation of the exported Random Forest tree structure.
 */
internal data class TreeData(
    val nodeCount: Int,
    val left: IntArray,
    val right: IntArray,
    val feature: IntArray,
    val threshold: DoubleArray,
    val leafProb: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TreeData
        return nodeCount == other.nodeCount &&
                left.contentEquals(other.left) &&
                right.contentEquals(other.right) &&
                feature.contentEquals(other.feature) &&
                threshold.contentEquals(other.threshold) &&
                leafProb.contentEquals(other.leafProb)
    }

    override fun hashCode(): Int {
        var result = nodeCount
        result = 31 * result + left.contentHashCode()
        result = 31 * result + right.contentHashCode()
        result = 31 * result + feature.contentHashCode()
        result = 31 * result + threshold.contentHashCode()
        result = 31 * result + leafProb.contentHashCode()
        return result
    }
}

/**
 * Frozen Stage 2 Random Forest model loaded from binary format v1.
 * Pure Kotlin/JVM with zero Android framework dependencies.
 */
internal class RiskModel(
    val version: Int,
    val nFeatures: Int,
    val hasScaler: Boolean,
    val scalerMean: DoubleArray?,
    val scalerScale: DoubleArray?,
    val nTrees: Int,
    val plattSlope: Double,
    val plattIntercept: Double,
    val clipLow: Double,
    val clipHigh: Double,
    val tau: Double,
    val trees: List<TreeData>
) {
    companion object {
        private const val MAGIC = "SHRM"
        private const val EXPECTED_VERSION = 1
        private const val EXPECTED_FEATURES = 5

        /**
         * Load binary model from an [InputStream].
         */
        fun load(inputStream: InputStream): RiskModel {
            val bytes = inputStream.readBytes()
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

            val magicBytes = ByteArray(4)
            buffer.get(magicBytes)
            val magic = String(magicBytes, Charsets.US_ASCII)
            if (magic != MAGIC) {
                throw IllegalArgumentException("Invalid model magic: $magic, expected $MAGIC")
            }

            val version = buffer.int
            if (version != EXPECTED_VERSION) {
                throw IllegalArgumentException("Unsupported model format version: $version")
            }

            val nFeatures = buffer.int
            if (nFeatures != EXPECTED_FEATURES) {
                throw IllegalArgumentException("Unexpected nFeatures: $nFeatures, expected $EXPECTED_FEATURES")
            }

            val hasScalerInt = buffer.int
            val hasScaler = hasScalerInt == 1

            val scalerMean = if (hasScaler) {
                DoubleArray(nFeatures) { buffer.double }
            } else null

            val scalerScale = if (hasScaler) {
                DoubleArray(nFeatures) { buffer.double }
            } else null

            val nTrees = buffer.int
            if (nTrees <= 0) {
                throw IllegalArgumentException("Invalid tree count: $nTrees")
            }

            val plattSlope = buffer.double
            val plattIntercept = buffer.double
            val clipLow = buffer.double
            val clipHigh = buffer.double
            val tau = buffer.double

            val trees = ArrayList<TreeData>(nTrees)
            for (t in 0 until nTrees) {
                val nodeCount = buffer.int
                if (nodeCount <= 0) {
                    throw IllegalArgumentException("Invalid nodeCount $nodeCount in tree $t")
                }

                val left = IntArray(nodeCount)
                for (i in 0 until nodeCount) left[i] = buffer.int

                val right = IntArray(nodeCount)
                for (i in 0 until nodeCount) right[i] = buffer.int

                val feature = IntArray(nodeCount)
                for (i in 0 until nodeCount) feature[i] = buffer.int

                val threshold = DoubleArray(nodeCount)
                for (i in 0 until nodeCount) threshold[i] = buffer.double

                val leafProb = DoubleArray(nodeCount)
                for (i in 0 until nodeCount) leafProb[i] = buffer.double

                trees.add(TreeData(nodeCount, left, right, feature, threshold, leafProb))
            }

            return RiskModel(
                version = version,
                nFeatures = nFeatures,
                hasScaler = hasScaler,
                scalerMean = scalerMean,
                scalerScale = scalerScale,
                nTrees = nTrees,
                plattSlope = plattSlope,
                plattIntercept = plattIntercept,
                clipLow = clipLow,
                clipHigh = clipHigh,
                tau = tau,
                trees = trees
            )
        }
    }
}

/**
 * Evaluates feature vectors using a loaded [RiskModel].
 */
internal class RiskEvaluator(private val model: RiskModel) {

    /**
     * Scores the 5 model features.
     * Crucial: Casts each feature to Float (float32) before comparing with threshold,
     * mirroring scikit-learn's internal tree traversal behavior.
     */
    fun evaluate(features: DoubleArray): RiskScore {
        require(features.size == model.nFeatures) {
            "Expected ${model.nFeatures} features, got ${features.size}"
        }

        val scaledFeatures = if (model.hasScaler && model.scalerMean != null && model.scalerScale != null) {
            DoubleArray(model.nFeatures) { i ->
                (features[i] - model.scalerMean[i]) / model.scalerScale[i]
            }
        } else {
            features
        }

        // Cast to Float32 for tree threshold comparisons (matches sklearn)
        val f32Features = FloatArray(model.nFeatures) { i -> scaledFeatures[i].toFloat() }

        var leafProbSum = 0.0
        for (tree in model.trees) {
            var node = 0
            while (tree.left[node] != -1) {
                val featIdx = tree.feature[node]
                val featValF32 = f32Features[featIdx]
                val thresh = tree.threshold[node]
                // Scikit-learn compares float32 X against float64 threshold
                node = if (featValF32.toDouble() <= thresh) {
                    tree.left[node]
                } else {
                    tree.right[node]
                }
            }
            leafProbSum += tree.leafProb[node]
        }

        val pRaw = leafProbSum / model.nTrees

        // Platt calibration
        val pClipped = max(model.clipLow, min(model.clipHigh, pRaw))
        val logit = ln(pClipped / (1.0 - pClipped))
        val z = model.plattSlope * logit + model.plattIntercept
        val pCal = 1.0 / (1.0 + exp(-z))

        // Confidence & abstention
        val confidence = max(pCal, 1.0 - pCal)
        val state = if (confidence >= model.tau) {
            "supported_prediction"
        } else {
            "uncertain_prediction"
        }

        val label = if (pCal >= 0.5) "risk" else "genuine"

        return RiskScore(
            pRaw = pRaw,
            pCal = pCal,
            state = state,
            label = label
        )
    }
}
