package com.example.bloomix

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

/**
 * COMPREHENSIVE EVALUATION ENGINE (Dual-Stratified)
 * --------------------------------------------------
 * 1. Loads and Deduplicates Data
 * 2. Splits 80/20 using STRATIFIED SAMPLING by CATEGORY + SENTIMENT
 * 3. Evaluates Naive Bayes (Sentiment & Category)
 * 4. Evaluates SVM (Category)
 * 5. Generates Reports: Feature Importance, Confusion Matrix, Classification Report
 */
object ModelEvaluator {

    private val CATEGORIES = listOf(
        "High-Energy Positive Focus",
        "Balanced and Contemplative",
        "Processing Difficult Emotions",
        "Complex Emotional Landscape"
    )

    private val SENTIMENTS = listOf("POSITIVE", "NEGATIVE", "NEUTRAL")

    fun runEvaluation(context: Context) {
        Log.d("ModelEval", "================================================")
        Log.d("ModelEval", "      STARTING FULL MODEL EVALUATION")
        Log.d("ModelEval", "================================================")

        val allData = loadJsonFromAssets(context, "training_data.json")
        val uniqueData = deduplicateData(allData)

        // Dual-stratified split: by CATEGORY + SENTIMENT
        val (trainSet, testSet) = splitDataDualStratified(uniqueData, 0.8)
        Log.d("ModelEval", "Data Split -> Train: ${trainSet.size}, Test: ${testSet.size}")

        // Print per-category and per-sentiment counts
        val trainCatCounts = trainSet.groupingBy { it.optString("category") }.eachCount()
        val testCatCounts = testSet.groupingBy { it.optString("category") }.eachCount()
        val trainSentCounts = trainSet.groupingBy { it.optString("sentiment") }.eachCount()
        val testSentCounts = testSet.groupingBy { it.optString("sentiment") }.eachCount()
        Log.d("ModelEval", "Train set category distribution: $trainCatCounts")
        Log.d("ModelEval", "Test set category distribution: $testCatCounts")
        Log.d("ModelEval", "Train set sentiment distribution: $trainSentCounts")
        Log.d("ModelEval", "Test set sentiment distribution: $testSentCounts")

        evaluateNaiveBayes(context, trainSet, testSet)
        evaluateSVM(trainSet, testSet)

        Log.d("ModelEval", "Restoring MLProcessor to full production state...")
        MLProcessor.initialize(context, null)
    }

    // ------------------------------------------------------------------------
    // NAIVE BAYES
    // ------------------------------------------------------------------------
    private fun evaluateNaiveBayes(context: Context, trainSet: List<JSONObject>, testSet: List<JSONObject>) {
        Log.d("ModelEval", "\n\n>>> MODEL A: NAIVE BAYES (MLProcessor) <<<")

        val trainArray = JSONArray()
        trainSet.forEach { trainArray.put(it) }
        MLProcessor.initialize(context, trainArray.toString())

        Log.d("ModelEval", "--- 4.3.1 Feature Importance (Naive Bayes) ---")
        val sampleText = trainSet.filter { it.optString("sentiment") == "POSITIVE" }
            .joinToString(" ") { it.getString("text") }
        val (topWords, _) = MLProcessor.extractKeyWords(sampleText)
        Log.d("ModelEval", "Top Predictive Features (Positive): $topWords")

        val sentimentMatrix = Array(3) { IntArray(3) }
        val categoryMatrix = Array(4) { IntArray(4) }

        testSet.forEach { obj ->
            val text = obj.getString("text")
            val emotions = jsonArrayToList(obj.getJSONArray("emotions"))

            val actualSentimentStr = obj.optString("sentiment", "NEUTRAL").uppercase()
            val actualCat = obj.optString("category", "Balanced and Contemplative")

            val result = MLProcessor.processEntry(text, emotions)

            // SENTIMENT MATRIX
            var sActIdx = SENTIMENTS.indexOf(actualSentimentStr).coerceAtLeast(2)
            var sPredIdx = SENTIMENTS.indexOf(result.sentiment.name).coerceAtLeast(2)
            sentimentMatrix[sActIdx][sPredIdx]++

            // CATEGORY MATRIX
            val cActIdx = CATEGORIES.indexOf(actualCat).coerceAtLeast(0)
            val cPredIdx = CATEGORIES.indexOf(result.overallMoodCategory).coerceAtLeast(0)
            categoryMatrix[cActIdx][cPredIdx]++
        }

        printReport("Naive Bayes Sentiment", SENTIMENTS.toTypedArray(), sentimentMatrix, testSet.size)
        printReport("Naive Bayes Category", CATEGORIES.toTypedArray(), categoryMatrix, testSet.size)
    }

    // ------------------------------------------------------------------------
    // SVM
    // ------------------------------------------------------------------------
    private fun evaluateSVM(trainSet: List<JSONObject>, testSet: List<JSONObject>) {
        Log.d("ModelEval", "\n\n>>> MODEL B: SUPPORT VECTOR MACHINE (SVM) <<<")

        val svm = MulticlassSVMClassifier(CATEGORIES)
        val allTexts = trainSet.map { it.getString("text") }
        svm.buildVocabulary(allTexts)

        Log.d("ModelEval", "Training SVM (${trainSet.size} samples)...")
        val trainDataTriples = trainSet.map { obj ->
            Triple(
                jsonArrayToList(obj.getJSONArray("emotions")),
                obj.getString("text"),
                obj.optString("category", "Balanced and Contemplative")
            )
        }
        svm.trainEpochs(trainDataTriples, epochs = 5)

        Log.d("ModelEval", "--- 4.3.1 Feature Importance (SVM) ---")
        Log.d("ModelEval", "SVM uses a dense feature vector of vocabulary words.")
        Log.d("ModelEval", "Total Features (Dimensions): ${allTexts.flatMap { TextPreprocessor.tokenize(it) }.toSet().size}")

        val categoryMatrix = Array(4) { IntArray(4) }
        testSet.forEach { obj ->
            val text = obj.getString("text")
            val emotions = jsonArrayToList(obj.getJSONArray("emotions"))
            val actualCat = obj.optString("category", "Balanced and Contemplative")
            val predictedCat = svm.predict(emotions, text)

            val actIdx = CATEGORIES.indexOf(actualCat).coerceAtLeast(0)
            val predIdx = CATEGORIES.indexOf(predictedCat).coerceAtLeast(0)
            categoryMatrix[actIdx][predIdx]++
        }

        printReport("SVM Category Classification", CATEGORIES.toTypedArray(), categoryMatrix, testSet.size)
    }

    // ------------------------------------------------------------------------
    // UTILITIES
    // ------------------------------------------------------------------------
    private fun printReport(title: String, labels: Array<String>, matrix: Array<IntArray>, total: Int) {
        val sb = StringBuilder()
        sb.append("\n============================================\n")
        sb.append("   $title REPORT\n")
        sb.append("============================================\n")

        sb.append("--- 4.3.2 Confusion Matrix ---\n")
        sb.append(String.format("%-15s", "Act \\ Pred"))
        labels.forEach { sb.append(String.format("[%7s] ", it.take(7))) }
        sb.append("\n")

        for (i in matrix.indices) {
            sb.append(String.format("%-15s", labels[i].take(15)))
            for (j in matrix.indices) sb.append(String.format("[%7d] ", matrix[i][j]))
            sb.append("\n")
        }

        sb.append("\n--- 4.3.4 Classification Report ---\n")
        sb.append(String.format("%-25s %-10s %-10s %-10s\n", "Class", "Precision", "Recall", "F1-Score"))
        sb.append("----------------------------------------------------------\n")

        var correctTotal = 0
        for (i in matrix.indices) correctTotal += matrix[i][i]

        for (i in matrix.indices) {
            val tp = matrix[i][i].toDouble()
            val colSum = (0 until matrix.size).sumOf { matrix[it][i] }
            val rowSum = (0 until matrix.size).sumOf { matrix[i][it] }
            val precision = if (colSum > 0) tp / colSum else 0.0
            val recall = if (rowSum > 0) tp / rowSum else 0.0
            val f1 = if (precision + recall > 0) 2 * (precision * recall) / (precision + recall) else 0.0
            sb.append(String.format("%-25s %-10.2f %-10.2f %-10.2f\n", labels[i].take(25), precision, recall, f1))
        }

        val acc = (correctTotal.toDouble() / total) * 100.0
        sb.append("----------------------------------------------------------\n")
        sb.append(String.format("OVERALL ACCURACY: %.2f%%\n", acc))
        sb.append("============================================\n")

        Log.i("ModelEval_Report", sb.toString())
    }

    private fun deduplicateData(raw: JSONArray): List<JSONObject> {
        val unique = ArrayList<JSONObject>()
        val seen = HashSet<String>()
        for (i in 0 until raw.length()) {
            val obj = raw.getJSONObject(i)
            val text = obj.getString("text")
            if (!seen.contains(text)) {
                seen.add(text)
                unique.add(obj)
            }
        }
        return unique
    }

    /**
     * Dual-stratified split by CATEGORY + SENTIMENT
     */
    private fun splitDataDualStratified(data: List<JSONObject>, ratio: Double): Pair<List<JSONObject>, List<JSONObject>> {
        // Group by category + sentiment
        val grouped = data.groupBy {
            val cat = it.optString("category", "Balanced and Contemplative")
            val sent = it.optString("sentiment", "NEUTRAL").uppercase()
            "$cat|$sent"
        }

        val trainList = mutableListOf<JSONObject>()
        val testList = mutableListOf<JSONObject>()
        val fixedRandom = Random(42)

        grouped.forEach { (_, groupData) ->
            val shuffled = groupData.shuffled(fixedRandom)
            val trainSize = (shuffled.size * ratio).toInt()
            trainList.addAll(shuffled.take(trainSize))
            testList.addAll(shuffled.drop(trainSize))
        }

        return Pair(trainList.shuffled(fixedRandom), testList.shuffled(fixedRandom))
    }

    private fun loadJsonFromAssets(context: Context, fileName: String): JSONArray {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val text = reader.use { it.readText() }
        return JSONArray(text)
    }

    private fun jsonArrayToList(arr: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.getString(i))
        return list
    }
}
