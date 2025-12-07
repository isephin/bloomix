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

    // Define the specific mood categories the app supports.
    private val CATEGORIES = listOf(
        "High-Energy Positive Focus",
        "Balanced and Contemplative",
        "Processing Difficult Emotions",
        "Complex Emotional Landscape"
    )

    // Define the sentiment levels.
    private val SENTIMENTS = listOf("POSITIVE", "NEGATIVE", "NEUTRAL")

    // --- MAIN EXECUTION FUNCTION ---
    // This is the entry point called from the Activity (e.g., SignUp).
    // It orchestrates the entire evaluation pipeline: loading data, splitting it, running models, and restoring the state.
    fun runEvaluation(context: Context) {
        Log.d("ModelEval", "================================================")
        Log.d("ModelEval", "      STARTING FULL MODEL EVALUATION")
        Log.d("ModelEval", "================================================")

        // 1. Load raw data from the JSON file in assets.
        val allData = loadJsonFromAssets(context, "training_data.json")

        // 2. Remove duplicate entries to ensure the test is fair and not biased by repeated data.
        val uniqueData = deduplicateData(allData)

        // 3. Split data into Training (80%) and Testing (20%) sets.
        // We use "Dual-Stratified" splitting to ensure every category and sentiment is represented in both sets.
        val (trainSet, testSet) = splitDataDualStratified(uniqueData, 0.8)
        Log.d("ModelEval", "Data Split -> Train: ${trainSet.size}, Test: ${testSet.size}")

        // Log the distribution statistics to verify the split is balanced.
        val trainCatCounts = trainSet.groupingBy { it.optString("category") }.eachCount()
        val testCatCounts = testSet.groupingBy { it.optString("category") }.eachCount()
        val trainSentCounts = trainSet.groupingBy { it.optString("sentiment") }.eachCount()
        val testSentCounts = testSet.groupingBy { it.optString("sentiment") }.eachCount()
        Log.d("ModelEval", "Train set category distribution: $trainCatCounts")
        Log.d("ModelEval", "Test set category distribution: $testCatCounts")
        Log.d("ModelEval", "Train set sentiment distribution: $trainSentCounts")
        Log.d("ModelEval", "Test set sentiment distribution: $testSentCounts")

        // 4. Run evaluation for Naive Bayes (Sentiment & Category).
        evaluateNaiveBayes(context, trainSet, testSet)

        // 5. Run evaluation for SVM (Category).
        evaluateSVM(trainSet, testSet)

        // 6. Restore the MLProcessor to its default state so the app works normally after the test.
        Log.d("ModelEval", "Restoring MLProcessor to full production state...")
        MLProcessor.initialize(context, null)
    }

    // ------------------------------------------------------------------------
    // NAIVE BAYES EVALUATION
    // ------------------------------------------------------------------------
    // Trains the Naive Bayes model on the specific trainSet and tests it against the testSet.
    private fun evaluateNaiveBayes(context: Context, trainSet: List<JSONObject>, testSet: List<JSONObject>) {
        Log.d("ModelEval", "\n\n>>> MODEL A: NAIVE BAYES (MLProcessor) <<<")

        // Convert the list of JSONObjects back to a JSONArray to initialize the MLProcessor.
        val trainArray = JSONArray()
        trainSet.forEach { trainArray.put(it) }

        // Re-initialize MLProcessor with ONLY the training data.
        MLProcessor.initialize(context, trainArray.toString())

        // Feature Importance: Check what words the model thinks are "Positive".
        // This helps verify if the model is learning relevant words or just noise.
        Log.d("ModelEval", "--- 4.3.1 Feature Importance (Naive Bayes) ---")
        val sampleText = trainSet.filter { it.optString("sentiment") == "POSITIVE" }
            .joinToString(" ") { it.getString("text") }
        val (topWords, _) = MLProcessor.extractKeyWords(sampleText)
        Log.d("ModelEval", "Top Predictive Features (Positive): $topWords")

        // Initialize Confusion Matrices to track correct vs incorrect predictions.
        val sentimentMatrix = Array(3) { IntArray(3) }
        val categoryMatrix = Array(4) { IntArray(4) }

        // Iterate through the Test Set (data the model has NEVER seen).
        testSet.forEach { obj ->
            val text = obj.getString("text")
            val emotions = jsonArrayToList(obj.getJSONArray("emotions"))

            val actualSentimentStr = obj.optString("sentiment", "NEUTRAL").uppercase()
            val actualCat = obj.optString("category", "Balanced and Contemplative")

            // Run prediction.
            val result = MLProcessor.processEntry(text, emotions)

            // --- POPULATE SENTIMENT MATRIX ---
            // Map string labels to array indices.
            // FIX: Don't use coerceAtLeast(2) because index 0 (POSITIVE) and 1 (NEGATIVE) are valid!
            var sActIdx = SENTIMENTS.indexOf(actualSentimentStr)
            if (sActIdx == -1) sActIdx = 2 // Default to NEUTRAL if unknown

            var sPredIdx = SENTIMENTS.indexOf(result.sentiment.name)
            if (sPredIdx == -1) sPredIdx = 2 // Default to NEUTRAL if unknown

            sentimentMatrix[sActIdx][sPredIdx]++

            // --- POPULATE CATEGORY MATRIX ---
            var cActIdx = CATEGORIES.indexOf(actualCat)
            if (cActIdx == -1) cActIdx = 1 // Default to Balanced (index 1) if unknown

            var cPredIdx = CATEGORIES.indexOf(result.overallMoodCategory)
            if (cPredIdx == -1) cPredIdx = 1 // Default to Balanced

            categoryMatrix[cActIdx][cPredIdx]++
        }

        // Print the final performance reports to Logcat.
        printReport("Naive Bayes Sentiment", SENTIMENTS.toTypedArray(), sentimentMatrix, testSet.size)
        printReport("Naive Bayes Category", CATEGORIES.toTypedArray(), categoryMatrix, testSet.size)
    }

    // ------------------------------------------------------------------------
    // SVM EVALUATION
    // ------------------------------------------------------------------------
    // Trains the Support Vector Machine (SVM) model on the trainSet and tests it.
    private fun evaluateSVM(trainSet: List<JSONObject>, testSet: List<JSONObject>) {
        Log.d("ModelEval", "\n\n>>> MODEL B: SUPPORT VECTOR MACHINE (SVM) <<<")

        // Initialize a fresh SVM instance.
        val svm = MulticlassSVMClassifier(CATEGORIES)

        // Build vocabulary from the training texts.
        val allTexts = trainSet.map { it.getString("text") }
        svm.buildVocabulary(allTexts)

        // Prepare training data tuples (Emotions + Text -> Category).
        Log.d("ModelEval", "Training SVM (${trainSet.size} samples)...")
        val trainDataTriples = trainSet.map { obj ->
            Triple(
                jsonArrayToList(obj.getJSONArray("emotions")),
                obj.getString("text"),
                obj.optString("category", "Balanced and Contemplative")
            )
        }

        // Train the SVM for 5 epochs (iterations) to let it converge.
        svm.trainEpochs(trainDataTriples, epochs = 5)

        // Log some debug info about the SVM's internal state.
        Log.d("ModelEval", "--- 4.3.1 Feature Importance (SVM) ---")
        Log.d("ModelEval", "SVM uses a dense feature vector of vocabulary words.")
        Log.d("ModelEval", "Total Features (Dimensions): ${allTexts.flatMap { TextPreprocessor.tokenize(it) }.toSet().size}")

        // Test the SVM on the unseen Test Set.
        val categoryMatrix = Array(4) { IntArray(4) }
        testSet.forEach { obj ->
            val text = obj.getString("text")
            val emotions = jsonArrayToList(obj.getJSONArray("emotions"))
            val actualCat = obj.optString("category", "Balanced and Contemplative")

            // Predict using the SVM.
            val predictedCat = svm.predict(emotions, text)

            // Populate Confusion Matrix.
            val actIdx = CATEGORIES.indexOf(actualCat).coerceAtLeast(0)
            val predIdx = CATEGORIES.indexOf(predictedCat).coerceAtLeast(0)
            categoryMatrix[actIdx][predIdx]++
        }

        // Print the report.
        printReport("SVM Category Classification", CATEGORIES.toTypedArray(), categoryMatrix, testSet.size)
    }

    // ------------------------------------------------------------------------
    // UTILITIES & REPORTING
    // ------------------------------------------------------------------------
    // Helper function to calculate Accuracy, Precision, Recall, and F1-Score
    // and format them into a readable text table in the logs.
    private fun printReport(title: String, labels: Array<String>, matrix: Array<IntArray>, total: Int) {
        val sb = StringBuilder()
        sb.append("\n============================================\n")
        sb.append("   $title REPORT\n")
        sb.append("============================================\n")

        // 1. Confusion Matrix Table
        sb.append("--- 4.3.2 Confusion Matrix ---\n")
        sb.append(String.format("%-15s", "Act \\ Pred"))
        labels.forEach { sb.append(String.format("[%7s] ", it.take(7))) }
        sb.append("\n")

        for (i in matrix.indices) {
            sb.append(String.format("%-15s", labels[i].take(15)))
            for (j in matrix.indices) sb.append(String.format("[%7d] ", matrix[i][j]))
            sb.append("\n")
        }

        // 2. Classification Metrics (Precision/Recall/F1)
        sb.append("\n--- 4.3.4 Classification Report ---\n")
        sb.append(String.format("%-25s %-10s %-10s %-10s\n", "Class", "Precision", "Recall", "F1-Score"))
        sb.append("----------------------------------------------------------\n")

        var correctTotal = 0
        for (i in matrix.indices) correctTotal += matrix[i][i]

        for (i in matrix.indices) {
            val tp = matrix[i][i].toDouble() // True Positive
            val colSum = (0 until matrix.size).sumOf { matrix[it][i] } // Predicted Total
            val rowSum = (0 until matrix.size).sumOf { matrix[i][it] } // Actual Total

            val precision = if (colSum > 0) tp / colSum else 0.0
            val recall = if (rowSum > 0) tp / rowSum else 0.0
            val f1 = if (precision + recall > 0) 2 * (precision * recall) / (precision + recall) else 0.0

            sb.append(String.format("%-25s %-10.2f %-10.2f %-10.2f\n", labels[i].take(25), precision, recall, f1))
        }

        // 3. Overall Accuracy
        val acc = (correctTotal.toDouble() / total) * 100.0
        sb.append("----------------------------------------------------------\n")
        sb.append(String.format("OVERALL ACCURACY: %.2f%%\n", acc))
        sb.append("============================================\n")

        Log.i("ModelEval_Report", sb.toString())
    }

    // Helper: Removes exact duplicate text entries to prevent data leakage.
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
     * Splits data into Train and Test sets while preserving the ratio of Categories and Sentiments.
     * This ensures we don't accidentally end up with all "Happy" entries in the Test set.
     */
    private fun splitDataDualStratified(data: List<JSONObject>, ratio: Double): Pair<List<JSONObject>, List<JSONObject>> {
        // Group by category + sentiment combo (e.g., "High-Energy|POSITIVE")
        val grouped = data.groupBy {
            val cat = it.optString("category", "Balanced and Contemplative")
            val sent = it.optString("sentiment", "NEUTRAL").uppercase()
            "$cat|$sent"
        }

        val trainList = mutableListOf<JSONObject>()
        val testList = mutableListOf<JSONObject>()
        val fixedRandom = Random(42) // Fixed seed for reproducibility

        grouped.forEach { (_, groupData) ->
            val shuffled = groupData.shuffled(fixedRandom)
            val trainSize = (shuffled.size * ratio).toInt()
            trainList.addAll(shuffled.take(trainSize))
            testList.addAll(shuffled.drop(trainSize))
        }

        return Pair(trainList.shuffled(fixedRandom), testList.shuffled(fixedRandom))
    }

    // Helper: Reads the JSON file from the assets folder.
    private fun loadJsonFromAssets(context: Context, fileName: String): JSONArray {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val text = reader.use { it.readText() }
        return JSONArray(text)
    }

    // Helper: Converts a JSONArray of strings into a Kotlin List<String>.
    private fun jsonArrayToList(arr: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.getString(i))
        return list
    }
}