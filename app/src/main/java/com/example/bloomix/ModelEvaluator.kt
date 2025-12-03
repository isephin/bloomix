package com.example.bloomix

import android.util.Log

/**
 * A Test Suite for the Machine Learning models.
 * This runs automatically when the app starts (called in SignUp.onCreate)
 * to verify that the Naive Bayes classifier is working correctly.
 */
object ModelEvaluator {

    // Simple data class to hold a test sentence and its "Correct" answer
    data class TestCase(
        val text: String,
        val expectedSentiment: Sentiment
    )

    // The 30 Test Sentences (10 Positive, 10 Negative, 10 Neutral)
    // We use these to calculate the "Confusion Matrix" (Accuracy Report).
    private val testSet = listOf(
        // --- POSITIVE SCENARIOS (10) ---
        TestCase("I had a truly wonderful and amazing time with my family today.", Sentiment.POSITIVE),
        TestCase("The project is finally finished and I feel so accomplished.", Sentiment.POSITIVE),
        TestCase("My heart feels peaceful and full of joy this morning.", Sentiment.POSITIVE),
        TestCase("I am energetic and ready to win the day.", Sentiment.POSITIVE),
        TestCase("Receiving that message made me feel loved and blessed.", Sentiment.POSITIVE),
        TestCase("I feel great and confident about my future plans.", Sentiment.POSITIVE),
        TestCase("A very productive day that ended with a relax evening.", Sentiment.POSITIVE),
        TestCase("Smiling because everything is going awesome lately.", Sentiment.POSITIVE),
        TestCase("I appreciate the support and feel thankful for my friends.", Sentiment.POSITIVE),
        TestCase("I enjoy looking forward to the success coming my way.", Sentiment.POSITIVE),

        // --- NEGATIVE SCENARIOS (10) ---
        TestCase("I am crying because I feel so lonely and hurt.", Sentiment.NEGATIVE),
        TestCase("This terrible headache is making me feel sick and broken.", Sentiment.NEGATIVE),
        TestCase("I hate how angry and annoyed I get at work.", Sentiment.NEGATIVE),
        TestCase("Feeling stressed and overwhelmed by the panic of deadlines.", Sentiment.NEGATIVE),
        TestCase("I failed the test and it feels awful and horrible.", Sentiment.NEGATIVE),
        TestCase("My mind is full of pain and I am just so tired.", Sentiment.NEGATIVE),
        TestCase("Nothing works and I am furious about the error.", Sentiment.NEGATIVE),
        TestCase("I feel stuck in a bored and empty routine.", Sentiment.NEGATIVE),
        TestCase("Everything is going bad and I am mad at myself.", Sentiment.NEGATIVE),
        TestCase("Scared and anxious about what will happen next.", Sentiment.NEGATIVE),

        // --- NEUTRAL SCENARIOS (10) ---
        TestCase("The day was okay, just a normal routine.", Sentiment.NEUTRAL),
        TestCase("Nothing special happened, just average work and sleep.", Sentiment.NEUTRAL),
        TestCase("I am feeling fine, not happy but not sad either.", Sentiment.NEUTRAL),
        TestCase("Just a standard day, I ate and went to bed.", Sentiment.NEUTRAL),
        TestCase("I feel mixed emotions, maybe a bit confused but fine.", Sentiment.NEUTRAL),
        TestCase("It was a weird day but mostly average.", Sentiment.NEUTRAL),
        TestCase("Routine is boring but it is safe and normal.", Sentiment.NEUTRAL),
        TestCase("I am unsure about tomorrow, perhaps it will be standard.", Sentiment.NEUTRAL),
        TestCase("Bittersweet feelings today, complicated but okay.", Sentiment.NEUTRAL),
        TestCase("Just another day passing by, nothing unusual.", Sentiment.NEUTRAL)
    )

    /**
     * Runs the test and prints the results to Android Logcat.
     * Search for "ModelEval" in the Logcat tab to see the output.
     */
    fun runEvaluation() {
        Log.d("ModelEval", "Starting Evaluation on ${testSet.size} sentences...")

        // Confusion Matrix: [Actual_Sentiment][Predicted_Sentiment]
        // Rows = Truth, Cols = Prediction
        // Index mapping: 0=Positive, 1=Negative, 2=Neutral
        val matrix = Array(3) { IntArray(3) }
        var totalCorrect = 0

        for (case in testSet) {
            // We pass an empty list of emotions because we are testing raw TEXT sentiment here
            val result = MLProcessor.processEntry(case.text, emptyList())
            val predicted = result.sentiment

            val actualIdx = getIndex(case.expectedSentiment)
            val predIdx = getIndex(predicted)

            // Increment the specific cell in the matrix
            matrix[actualIdx][predIdx]++

            if (actualIdx == predIdx) {
                totalCorrect++
            } else {
                // Log mistakes so we can fix the training data later
                Log.d("ModelEval", "MISMATCH: '${case.text}' | Expected: ${case.expectedSentiment}, Got: $predicted")
            }
        }

        // --- GENERATE & PRINT REPORT ---
        val accuracy = (totalCorrect.toDouble() / testSet.size) * 100
        val sb = StringBuilder()

        sb.append("\n============================================\n")
        sb.append("       MODEL EVALUATION REPORT\n")
        sb.append("============================================\n")
        sb.append(String.format("Accuracy: %.2f%%\n", accuracy))
        sb.append("--------------------------------------------\n")
        sb.append("CONFUSION MATRIX (Rows=Actual, Cols=Predicted)\n")
        sb.append("      [POS] [NEG] [NEU]\n")
        sb.append(String.format("POS   [%2d]  [%2d]  [%2d]\n", matrix[0][0], matrix[0][1], matrix[0][2]))
        sb.append(String.format("NEG   [%2d]  [%2d]  [%2d]\n", matrix[1][0], matrix[1][1], matrix[1][2]))
        sb.append(String.format("NEU   [%2d]  [%2d]  [%2d]\n", matrix[2][0], matrix[2][1], matrix[2][2]))
        sb.append("--------------------------------------------\n")
        sb.append("CLASSIFICATION REPORT\n")
        sb.append(String.format("%-10s %-10s %-10s %-10s\n", "Class", "Precision", "Recall", "F1-Score"))

        // Calculate advanced metrics (Precision/Recall) for each class
        printClassMetrics(sb, "Positive", 0, matrix)
        printClassMetrics(sb, "Negative", 1, matrix)
        printClassMetrics(sb, "Neutral",  2, matrix)

        sb.append("============================================\n")

        Log.i("ModelEval_Result", sb.toString())
    }

    // Helper to map Sentiment Enum to Integer Index (0, 1, 2)
    private fun getIndex(s: Sentiment): Int {
        return when(s) {
            Sentiment.POSITIVE -> 0
            Sentiment.NEGATIVE -> 1
            Sentiment.NEUTRAL -> 2
        }
    }

    // Helper to calculate Precision, Recall, and F1-Score
    private fun printClassMetrics(sb: StringBuilder, label: String, idx: Int, matrix: Array<IntArray>) {
        // TP (True Positive) = The diagonal value (Correct predictions)
        val tp = matrix[idx][idx].toDouble()

        // FP (False Positive) = Sum of the column (Predicted this class, but was wrong) - TP
        var colSum = 0
        for(r in 0..2) colSum += matrix[r][idx]
        val fp = (colSum - tp)

        // FN (False Negative) = Sum of the row (Actually this class, but predicted wrong) - TP
        var rowSum = 0
        for(c in 0..2) rowSum += matrix[idx][c]
        val fn = (rowSum - tp)

        val precision = if ((tp + fp) > 0) tp / (tp + fp) else 0.0
        val recall = if ((tp + fn) > 0) tp / (tp + fn) else 0.0
        val f1 = if ((precision + recall) > 0) 2 * (precision * recall) / (precision + recall) else 0.0

        sb.append(String.format("%-10s %-10.2f %-10.2f %-10.2f\n", label, precision, recall, f1))
    }
}