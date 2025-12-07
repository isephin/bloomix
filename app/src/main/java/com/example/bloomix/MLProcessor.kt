package com.example.bloomix

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Singleton object responsible for all Machine Learning operations in the app.
 * It manages the Naive Bayes Classifier (Sentiment) and the SVM Classifier (Mood Category).
 * It handles data loading, training, prediction, and feature extraction.
 */
object MLProcessor {

    // --- STATE MANAGEMENT ---
    // Flag to prevent reloading and retraining the models if they are already ready.
    // This optimization ensures the app doesn't freeze when navigating between screens.
    private var isInitialized = false

    // --- NAIVE BAYES VARIABLES (Sentiment Analysis) ---
    // wordCounts: Stores the frequency of each word for each Sentiment (Positive, Negative, Neutral).
    // e.g., { POSITIVE -> { "happy": 50, "love": 30 }, NEGATIVE -> { "sad": 40 } }
    private val wordCounts: MutableMap<Sentiment, MutableMap<String, Int>> = mutableMapOf()

    // docFreq: Tracks how many documents (entries) a word appears in total.
    // This is used for calculating IDF (Inverse Document Frequency).
    private val docFreq: MutableMap<String, Int> = mutableMapOf()

    // classCounts: Tracks the total number of documents belonging to each Sentiment class.
    private val classCounts: MutableMap<Sentiment, Int> = mutableMapOf()

    // vocab: A set of all unique words encountered during training.
    private val vocab: MutableSet<String> = mutableSetOf()

    // totalDocs: The total number of training examples processed.
    private var totalDocs = 0

    // --- SVM VARIABLES (Mood Categorization) ---
    // svmVocab: A selected list of top N most frequent words used as features for the SVM.
    // Unlike Naive Bayes which uses all words, SVM works better with a fixed, dense feature vector.
    private var svmVocab: List<String> = emptyList()

    // svmModels: A collection of LinearSVM instances, one for each category (One-vs-Rest strategy).
    private val svmModels = mutableMapOf<String, LinearSVM>()

    // The fixed list of mood categories the SVM is trained to predict.
    private val categories = listOf(
        "High-Energy Positive Focus",
        "Balanced and Contemplative",
        "Processing Difficult Emotions",
        "Complex Emotional Landscape"
    )

    // --- TUNABLE PARAMETERS ---
    // smoothingAlpha: Used in Naive Bayes to handle unseen words (Laplace Smoothing).
    // A value of 1.0 is standard.
    var smoothingAlpha: Double = 1.0

    // stopWords: A set of common, low-value words (like "the", "is") to filter out
    // so the models focus on meaningful content.
    private val stopWords = setOf(
        "the", "is", "at", "which", "on", "a", "an", "and", "or", "but",
        "of", "to", "in", "it", "i", "my", "me", "was", "with", "just", "for"
    )

    // --- INITIALIZATION ---
    // Called when the app starts. It loads data from assets and trains both models.
    fun initialize(context: Context, customData: String? = null) {
        // Safety check: Skip if already initialized (unless we are forcing new data for testing).
        if (isInitialized && customData == null) return

        try {
            // Reset all data structures to ensure a clean training state.
            clearData()

            // Load the training dataset (JSON) from the app's assets folder.
            // If 'customData' is provided (e.g., during evaluation), use that instead.
            val jsonString = customData ?: loadJsonFromAssets(context, "training_data.json")
            val jsonArray = JSONArray(jsonString)

            // 1. FIRST PASS: Train Naive Bayes & Prepare Data for SVM
            val allText = StringBuilder()

            // Temporary list to hold processed data for the SVM training phase.
            val svmTrainingData = mutableListOf<Triple<List<String>, String, String>>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val text = obj.getString("text")
                val sentimentStr = obj.optString("sentiment", "NEUTRAL")
                // Parse sentiment string to Enum, defaulting to NEUTRAL if invalid.
                val sentiment = try { Sentiment.valueOf(sentimentStr) } catch(e: Exception) { Sentiment.NEUTRAL }
                val category = obj.optString("category", "Balanced and Contemplative")
                val emotions = jsonArrayToList(obj.getJSONArray("emotions"))

                // Feed this entry to the Naive Bayes trainer.
                trainNaiveBayes(text, sentiment)

                // Accumulate text to build the SVM vocabulary later.
                allText.append(text).append(" ")
                // Store structured data for SVM training.
                svmTrainingData.add(Triple(emotions, text, category))
            }

            // 2. BUILD SVM VOCABULARY
            // Tokenize all text, count word frequencies, and keep the top 200 most common words.
            // This creates a manageable feature space for the Linear SVM.
            val tokens = tokenize(allText.toString())
            svmVocab = tokens.groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }
                .take(200)
                .map { it.key }

            // 3. INITIALIZE SVM MODELS
            // Create a LinearSVM instance for each category.
            // Feature size = Vocabulary Size + 3 (Sentiment Scores) + 0 (Emotions handled via mapping).
            categories.forEach { cat ->
                svmModels[cat] = LinearSVM(svmVocab.size + 3)
            }

            // 4. TRAIN SVM MODELS
            // Run 5 training epochs (iterations) over the dataset.
            // Shuffling ensures the model doesn't overfit to the order of data.
            for (epoch in 0 until 5) {
                svmTrainingData.shuffled().forEach { (emotions, text, targetCat) ->
                    trainSVM(emotions, text, targetCat)
                }
            }

            // Mark initialization as complete.
            isInitialized = true
            Log.d("MLProcessor", "Training Complete. Docs: $totalDocs, Vocab: ${vocab.size}, SVM Vocab: ${svmVocab.size}")
        } catch (e: Exception) {
            Log.e("MLProcessor", "Error loading training data: ${e.message}")
            e.printStackTrace()
        }
    }

    // Resets all internal maps and counters.
    private fun clearData() {
        wordCounts.clear()
        docFreq.clear()
        classCounts.clear()
        vocab.clear()
        svmModels.clear()
        svmVocab = emptyList()
        totalDocs = 0
        Sentiment.values().forEach {
            classCounts[it] = 0
            wordCounts[it] = mutableMapOf()
        }
    }

    // --- NAIVE BAYES TRAINING LOGIC ---
    // Updates word counts and document frequencies for a single training example.
    private fun trainNaiveBayes(text: String, sentiment: Sentiment) {
        val tokens = tokenize(text)
        val uniqueTokens = tokens.toSet() // Use set to count DF (Document Frequency) only once per doc

        // Increment class count (e.g., one more POSITIVE document)
        classCounts[sentiment] = (classCounts[sentiment] ?: 0) + 1
        totalDocs++

        // Update word counts for this specific sentiment class
        val countsMap = wordCounts[sentiment] ?: return
        for (token in tokens) {
            vocab.add(token)
            countsMap[token] = countsMap.getOrDefault(token, 0) + 1
        }

        // Update global document frequency for each word
        for (token in uniqueTokens) {
            docFreq[token] = docFreq.getOrDefault(token, 0) + 1
        }
    }

    // --- SVM FEATURE EXTRACTION ---
    // Converts a text input + emotions into a numerical feature vector (DoubleArray).
    // This is the "Input" that the SVM mathematical model understands.
    private fun extractSVMFeatures(emotions: List<String>, text: String): DoubleArray {
        // 1. TEXT FEATURES (Bag of Words)
        // Check presence of top 200 vocabulary words in the text.
        val tokens = tokenize(text)
        val features = DoubleArray(svmVocab.size + 3) // +3 slots for Sentiment Scores

        for (i in svmVocab.indices) {
            if (tokens.contains(svmVocab[i])) {
                features[i] = 1.0 // Set to 1.0 if word exists
            }
        }

        // 2. SENTIMENT FEATURES (The "Hint")
        // Run a Naive Bayes prediction to get probability scores for Positive, Negative, Neutral.
        // These scores are added as features to help the SVM distinguish mood nuances.
        val sentimentScores = getSentimentScores(text)
        // Append probabilities to the end of the feature vector
        features[svmVocab.size] = sentimentScores[Sentiment.POSITIVE] ?: 0.0
        features[svmVocab.size + 1] = sentimentScores[Sentiment.NEGATIVE] ?: 0.0
        features[svmVocab.size + 2] = sentimentScores[Sentiment.NEUTRAL] ?: 0.0

        // 3. NORMALIZATION (L2 Norm)
        // Scales the vector so it has a length of 1. This helps the SVM converge faster.
        val norm = sqrt(features.fold(0.0) { acc, v -> acc + v * v })
        if (norm > 0.0) {
            for (i in features.indices) features[i] /= norm
        }
        return features
    }

    // --- SVM TRAINING ---
    // Trains all category models on a single example.
    private fun trainSVM(emotions: List<String>, text: String, targetCategory: String) {
        val features = extractSVMFeatures(emotions, text)
        categories.forEach { cat ->
            // Binary Classification: Label is 1 if it matches the target category, -1 otherwise.
            val label = if (cat == targetCategory) 1 else -1
            svmModels[cat]?.train(features, label)
        }
    }

    // --- SVM PREDICTION ---
    // Uses the trained SVM models to predict the best mood category.
    private fun predictCategorySVM(emotions: List<String>, text: String): String {
        val features = extractSVMFeatures(emotions, text)
        var bestCat = categories[0]
        var bestScore = -Double.MAX_VALUE

        // Ask each model for a score; the highest score wins.
        categories.forEach { cat ->
            val score = svmModels[cat]?.predict(features) ?: -Double.MAX_VALUE
            if (score > bestScore) {
                bestScore = score
                bestCat = cat
            }
        }
        return bestCat
    }

    // --- TOKENIZATION UTILS ---
    // Cleans raw text: lowercase, remove non-alpha characters, filter stopwords, and apply Stemming.
    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z ]"), "")
            .split(" ")
            .filter { it.isNotBlank() && !stopWords.contains(it) }
            .map { PorterStemmer.stem(it) } // Reduces words to root form (e.g., running -> run)
    }

    // --- MAIN PUBLIC ENTRY POINT ---
    // Called by the UI (JournalActivity) to analyze a new entry.
    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {
        // 1. Predict Sentiment using Naive Bayes
        val sentiment = predictSentiment(journalText, selectedEmotions)

        // 2. Predict Category using SVM (which also uses the Sentiment result internally)
        val category = predictCategorySVM(selectedEmotions, journalText)

        // 3. Generate actionable advice based on the results
        return AnalysisResult(
            sentiment = sentiment,
            overallMoodCategory = category,
            reflectionPrompt = generateReflection(category, sentiment),
            suggestedMicroActions = listOf(MicroAction("Recommended", generateMicroAction(category)))
        )
    }

    // --- SENTIMENT PREDICTION (Helper) ---
    // Returns the Sentiment enum with the highest probability score.
    private fun predictSentiment(text: String, emotions: List<String>): Sentiment {
        val scores = getSentimentScores(text, emotions)
        return scores.maxByOrNull { it.value }?.key ?: Sentiment.NEUTRAL
    }

    /**
     * Calculates the raw Log-Likelihood scores for each Sentiment class.
     * Uses the Naive Bayes formula: P(Class|Words) ~ P(Class) * P(Word1|Class) * P(Word2|Class)...
     */
    private fun getSentimentScores(text: String, emotions: List<String> = emptyList()): Map<Sentiment, Double> {
        if (totalDocs == 0) return mapOf(Sentiment.NEUTRAL to 0.0)

        // Combine user text and selected emotions into one string for analysis.
        val textToProcess = if (emotions.isNotEmpty()) "$text ${emotions.joinToString(" ")}" else text
        val tokens = tokenize(textToProcess)

        val scores = mutableMapOf<Sentiment, Double>()

        for (sentiment in Sentiment.values()) {
            val classDocCount = classCounts[sentiment] ?: 0
            // Prior Probability P(Class)
            val pClass = ln(classDocCount.toDouble() / totalDocs)
            var logProbWords = 0.0
            val classTermMap = wordCounts[sentiment]!!
            val totalTermsInClass = classTermMap.values.sum()

            for (token in tokens) {
                if (vocab.contains(token)) {
                    val tf = classTermMap.getOrDefault(token, 0).toDouble()
                    // Laplace Smoothing: (Count + Alpha) / (TotalTerms + Alpha * VocabSize)
                    val numerator = tf + smoothingAlpha
                    val denominator = totalTermsInClass + (smoothingAlpha * vocab.size)
                    logProbWords += ln(numerator / denominator)
                }
            }
            // Final Score = Log(Prior) + Sum(Log(Likelihoods))
            scores[sentiment] = pClass + logProbWords
        }
        return scores
    }

    // --- LINEAR SVM IMPLEMENTATION ---
    // A simple implementation of a Linear Support Vector Machine using Gradient Descent.
    class LinearSVM(private val numFeatures: Int, private val learningRate: Double = 0.01) {
        private val weights = DoubleArray(numFeatures) { 0.0 }
        private var bias = 0.0

        // Stochastic Gradient Descent (SGD) training step.
        // label: 1 (Positive class) or -1 (Negative class).
        fun train(features: DoubleArray, label: Int) {
            val y = label.toDouble()
            // Calculate prediction: w * x + b
            val dot = features.foldIndexed(0.0) { i, acc, v -> acc + v * weights[i] } + bias

            // Hinge Loss check: If prediction is wrong or within margin (1.0)...
            if (y * dot < 1.0) {
                // Update weights to correct the error
                for (i in weights.indices) {
                    weights[i] += learningRate * (y * features[i] - 0.01 * weights[i]) // Includes regularization
                }
                bias += learningRate * y
            } else {
                // Just apply regularization decay if correct
                for (i in weights.indices) {
                    weights[i] -= learningRate * 0.01 * weights[i]
                }
            }
        }

        // Returns the raw score (distance from hyperplane).
        fun predict(features: DoubleArray): Double {
            return features.foldIndexed(0.0) { i, acc, v -> acc + v * weights[i] } + bias
        }
    }

    // --- JSON PARSING HELPER ---
    private fun jsonArrayToList(arr: JSONArray): List<String> {
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) out.add(arr.getString(i))
        return out
    }

    // --- REFLECTION & MICRO-ACTION GENERATORS ---
    // Hardcoded responses mapped to the detected category.

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

    // Used for the Stats screen to find frequent words (optional feature).
    fun extractKeyWords(text: String): Pair<List<String>, List<String>> {
        val tokens = tokenize(text)
        val freqs = tokens.groupingBy { it }.eachCount()
        val topWords = freqs.entries.sortedByDescending { it.value }.take(5).map { it.key }
        return Pair(topWords, emptyList())
    }

    // Reads the JSON file from the assets folder.
    private fun loadJsonFromAssets(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }
}