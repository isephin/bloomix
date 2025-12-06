package com.example.bloomix

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.ln

object MLProcessor {

    // --- NAIVE BAYES VARIABLES ---
    // Maps Sentiment -> Word -> Frequency Count (TF)
    private val wordCounts: MutableMap<Sentiment, MutableMap<String, Int>> = mutableMapOf()

    // Maps Word -> Number of documents it appears in (DF - for IDF calc)
    private val docFreq: MutableMap<String, Int> = mutableMapOf()

    private val classCounts: MutableMap<Sentiment, Int> = mutableMapOf()
    private val vocab: MutableSet<String> = mutableSetOf()
    private var totalDocs = 0

    // --- TUNABLE PARAMETERS ---
    // Laplace Smoothing Alpha (default 1.0).
    // Optimization function can change this to find the best value.
    var smoothingAlpha: Double = 1.0

    // Stop words to ignore
    private val stopWords = setOf(
        "the", "is", "at", "which", "on", "a", "an", "and", "or", "but",
        "of", "to", "in", "it", "i", "my", "me", "was", "with", "just", "for"
    )

    fun initialize(context: Context, customData: String? = null) {
        try {
            clearData()

            // Use customData if provided (by Evaluator), otherwise load from file
            val jsonString = customData ?: loadJsonFromAssets(context, "training_data.json")
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val text = obj.getString("text")
                val sentimentStr = obj.optString("sentiment", "NEUTRAL")
                val sentiment = try { Sentiment.valueOf(sentimentStr) } catch(e: Exception) { Sentiment.NEUTRAL }

                train(text, sentiment)
            }
            Log.d("MLProcessor", "Training Complete. Docs: $totalDocs, Vocab: ${vocab.size}")
        } catch (e: Exception) {
            Log.e("MLProcessor", "Error loading training data: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun clearData() {
        wordCounts.clear()
        docFreq.clear()
        classCounts.clear()
        vocab.clear()
        totalDocs = 0
        Sentiment.values().forEach {
            classCounts[it] = 0
            wordCounts[it] = mutableMapOf()
        }
    }

    private fun train(text: String, sentiment: Sentiment) {
        val tokens = tokenize(text) // Now uses Stemming!
        val uniqueTokens = tokens.toSet() // For Doc Frequency

        classCounts[sentiment] = (classCounts[sentiment] ?: 0) + 1
        totalDocs++

        // 1. Update Term Frequency (TF) per class
        val countsMap = wordCounts[sentiment] ?: return
        for (token in tokens) {
            vocab.add(token)
            countsMap[token] = countsMap.getOrDefault(token, 0) + 1
        }

        // 2. Update Document Frequency (DF) global
        for (token in uniqueTokens) {
            docFreq[token] = docFreq.getOrDefault(token, 0) + 1
        }
    }

    /**
     * Applies Lowercasing + Regex Cleaning + Stopword Removal + PORTER STEMMING
     */
    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z ]"), "")
            .split(" ")
            .filter { it.isNotBlank() && !stopWords.contains(it) }
            .map { PorterStemmer.stem(it) } // Apply Stemming
    }

    // --- MAIN ENTRY POINT ---
    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {
        val sentiment = predictSentiment(journalText, selectedEmotions)
        val category = determineWeightedCategory(sentiment, selectedEmotions)

        return AnalysisResult(
            sentiment = sentiment,
            overallMoodCategory = category,
            reflectionPrompt = generateReflection(category, sentiment),
            suggestedMicroActions = listOf(MicroAction("Recommended", generateMicroAction(category)))
        )
    }

    /**
     * PREDICT SENTIMENT USING TF-IDF + LAPLACE SMOOTHING
     */
    private fun predictSentiment(text: String, emotions: List<String>): Sentiment {
        if (totalDocs == 0) return Sentiment.NEUTRAL

        // Combine text + emotion tags for better context
        val combinedText = "$text ${emotions.joinToString(" ")}"
        val tokens = tokenize(combinedText)

        var bestSentiment = Sentiment.NEUTRAL
        var maxProb = Double.NEGATIVE_INFINITY

        for (sentiment in Sentiment.values()) {
            val classDocCount = classCounts[sentiment] ?: 0
            // Prior P(Class)
            val pClass = ln(classDocCount.toDouble() / totalDocs)

            var logProbWords = 0.0
            val classTermMap = wordCounts[sentiment]!!

            // Total words in this class (for denominator)
            val totalTermsInClass = classTermMap.values.sum()

            for (token in tokens) {
                if (vocab.contains(token)) {
                    // --- TF-IDF CALCULATION ---

                    // TF: Frequency of word in this specific class
                    val tf = classTermMap.getOrDefault(token, 0).toDouble()

                    // IDF: log( TotalDocs / (DocFreq + 1) ) + 1
                    val df = docFreq.getOrDefault(token, 0)
                    val idf = ln((totalDocs + 1.0) / (df + 1.0)) + 1.0

                    // Weighted Weight
                    val weightedCount = tf * idf

                    // --- LAPLACE SMOOTHING WITH ALPHA ---
                    // P(Word|Class) = (WeightedCount + Alpha) / (TotalTerms + Alpha * VocabSize)
                    val numerator = weightedCount + smoothingAlpha
                    val denominator = totalTermsInClass + (smoothingAlpha * vocab.size)

                    logProbWords += ln(numerator / denominator)
                }
            }

            val totalProb = pClass + logProbWords
            if (totalProb > maxProb) {
                maxProb = totalProb
                bestSentiment = sentiment
            }
        }
        return bestSentiment
    }

    // --- HYPERPARAMETER OPTIMIZATION ---
    /**
     * Call this from ModelEvaluator to find the best Alpha.
     * Returns a string describing the results.
     */
    fun optimizeAlpha(testData: JSONArray): String {
        val alphasToTest = listOf(0.1, 0.5, 1.0, 1.5, 2.0, 5.0)
        val sb = StringBuilder()
        sb.append("Optimizing Alpha:\n")

        var bestAlpha = 1.0
        var bestAccuracy = 0.0

        for (alpha in alphasToTest) {
            smoothingAlpha = alpha
            var correct = 0

            for (i in 0 until testData.length()) {
                val obj = testData.getJSONObject(i)
                val text = obj.getString("text")
                val emotions = jsonArrayToList(obj.getJSONArray("emotions"))
                val expectedStr = obj.optString("sentiment", "NEUTRAL")

                // Use internal logic directly
                val predicted = predictSentiment(text, emotions)
                if (predicted.name == expectedStr) correct++
            }

            val acc = (correct.toDouble() / testData.length()) * 100.0
            sb.append("Alpha $alpha -> Acc: ${"%.2f".format(acc)}%\n")

            if (acc > bestAccuracy) {
                bestAccuracy = acc
                bestAlpha = alpha
            }
        }

        // Set the model to use the winner
        smoothingAlpha = bestAlpha
        sb.append(">> Winner: Alpha $bestAlpha")
        return sb.toString()
    }

    private fun jsonArrayToList(arr: JSONArray): List<String> {
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) out.add(arr.getString(i))
        return out
    }

    // --- LOGIC FIX APPLIED HERE ---
    private fun determineWeightedCategory(sentiment: Sentiment, emotions: List<String>): String {
        var highEnergyScore = 0
        var balancedScore = 0
        var difficultScore = 0
        var complexScore = 0

        // 1. Tally scores from emotion tags
        for (emo in emotions) {
            when (emo.lowercase(Locale.getDefault())) {
                "happy", "excited", "loved", "shocked", "pumped", "proud" -> highEnergyScore++
                "calm", "bored", "steady", "peaceful", "relaxed" -> balancedScore++
                "sad", "angry", "tired", "stressed", "annoyed", "drained" -> difficultScore++
                "confused", "mixed" -> complexScore += 2
            }
        }

        // 2. Apply sentiment score boost
        when (sentiment) {
            Sentiment.POSITIVE -> highEnergyScore += 2
            Sentiment.NEGATIVE -> difficultScore += 2
            Sentiment.NEUTRAL -> { balancedScore += 2; complexScore += 1 }
        }

        // 3. Complex Emotion check (High-Energy and Difficult present)
        if (highEnergyScore >= 1 && difficultScore >= 1) return "Complex Emotional Landscape"

        // --- NEW TIE-BREAKING LOGIC ---
        // 4. Find the winning category using explicit precedence.
        // We prioritize Difficult > High-Energy > Complex > Balanced (as the default/tie-breaker).
        // This avoids the 'else' trap from the original code.

        val scores = listOf(difficultScore, highEnergyScore, complexScore, balancedScore)
        val maxScore = scores.maxOrNull() ?: 0

        // Find the index of the *first* score that equals the maximum score.
        val winningIndex = scores.indexOfFirst { it == maxScore }

        return when (winningIndex) {
            0 -> "Processing Difficult Emotions"
            1 -> "High-Energy Positive Focus"
            2 -> "Complex Emotional Landscape"
            3 -> "Balanced and Contemplative"
            else -> "Balanced and Contemplative" // Default for safety (e.g., if maxScore was 0)
        }
    }
    // ------------------------------------

    private fun generateReflection(category: String, sentiment: Sentiment): String {
        return when (category) {
            "High-Energy Positive Focus" -> "You're thriving! What fueled this energy today?"
            "Processing Difficult Emotions" -> "It's okay to feel this way. What is one small step to help you cope?"
            "Complex Emotional Landscape" -> "There's a lot going on. Can you untangle just one specific feeling?"
            else -> "A steady day. What brought you a sense of peace?"
        }
    }

    private fun generateMicroAction(category: String): String {
        return when (category) {
            "High-Energy Positive Focus" -> "Share your win with a friend."
            "Balanced and Contemplative" -> "Spend 5 minutes observing nature."
            "Processing Difficult Emotions" -> "Drink a glass of water and stretch."
            "Complex Emotional Landscape" -> "Listen to a song that comforts you."
            else -> "Take a deep breath."
        }
    }

    fun extractKeyWords(text: String): Pair<List<String>, List<String>> {
        val tokens = tokenize(text)
        val freqs = tokens.groupingBy { it }.eachCount()
        val topWords = freqs.entries.sortedByDescending { it.value }.take(5).map { it.key }
        return Pair(topWords, emptyList())
    }

    private fun loadJsonFromAssets(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }
}