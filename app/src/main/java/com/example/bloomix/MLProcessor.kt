package com.example.bloomix

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Singleton object responsible for all Machine Learning operations in the app.
 *
 * REFACTORED:
 * This class now acts as a "Controller" or "Orchestrator".
 * Instead of doing the math itself, it delegates the heavy lifting to:
 * 1. MachineLearning.kt (NaiveBayesClassifier, MulticlassSVMClassifier, TextPreprocessor)
 * 2. ReflectionData.kt (Smart prompt matching)
 */
object MLProcessor {

    // --- STATE MANAGEMENT ---
    // Flag to prevent reloading and retraining the models if they are already ready.
    // This optimization ensures the app doesn't freeze when navigating between screens.
    private var isInitialized = false

    // --- AI MODELS ---
    // We now use the classes defined in MachineLearning.kt to handle the actual math.
    // nbClassifier: Handles Sentiment Analysis (Positive/Negative/Neutral).
    private val nbClassifier = NaiveBayesClassifier()

    // svmClassifier: Handles Mood Categorization (e.g., "High-Energy Positive Focus").
    private val svmClassifier = MulticlassSVMClassifier(listOf(
        "High-Energy Positive Focus",
        "Balanced and Contemplative",
        "Processing Difficult Emotions",
        "Complex Emotional Landscape"
    ))

    // --- INITIALIZATION ---
    // Called when the app starts. It loads data from assets and trains both models.
    fun initialize(context: Context, customData: String? = null) {
        // Safety check: Skip if already initialized (unless we are forcing new data for testing).
        if (isInitialized && customData == null) return

        try {
            // 1. Load Data
            // Load the training dataset (JSON) from the app's assets folder.
            val jsonString = customData ?: loadJsonFromAssets(context, "training_data.json")
            val jsonArray = JSONArray(jsonString)

            // Temporary lists to hold processed data for the training phase.
            val svmTrainingData = mutableListOf<Triple<List<String>, String, String>>()
            val allTexts = mutableListOf<String>()

            // 2. Train Naive Bayes & Prepare SVM Data
            // Iterate through every entry in the JSON training data.
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val text = obj.getString("text")
                val sentimentStr = obj.optString("sentiment", "NEUTRAL")
                val category = obj.optString("category", "Balanced and Contemplative")
                val emotions = jsonArrayToList(obj.getJSONArray("emotions"))

                // Parse Sentiment string to Enum
                val sentiment = try { Sentiment.valueOf(sentimentStr) } catch(e: Exception) { Sentiment.NEUTRAL }

                // Train Naive Bayes (Logic is now delegated to MachineLearning.kt)
                // Note: We combine text + emotions into one string to give the sentiment classifier more context.
                val textWithEmotions = "$text ${emotions.joinToString(" ")}"
                nbClassifier.train(textWithEmotions, sentiment)

                // Store structured data to train the SVM later
                svmTrainingData.add(Triple(emotions, text, category))
                allTexts.add(text)
            }

            // 3. Finalize Models
            // Calculate TF-IDF probabilities for Naive Bayes
            nbClassifier.finalizeTraining()

            // Train SVM (Logic is now delegated to MachineLearning.kt)
            // First, build a vocabulary of common words to use as features.
            svmClassifier.buildVocabulary(allTexts)
            // Then, run training epochs (iterations) to teach the model to distinguish categories.
            svmClassifier.trainEpochs(svmTrainingData, epochs = 5)

            isInitialized = true
            Log.d("MLProcessor", "Refactored Initialization Complete.")

        } catch (e: Exception) {
            Log.e("MLProcessor", "Error loading training data: ${e.message}")
            e.printStackTrace()
        }
    }

    // --- MAIN PUBLIC ENTRY POINT ---
    // Called by the UI (JournalActivity) to analyze a new entry.
    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {
        // 1. Predict Sentiment (Delegated to NaiveBayesClassifier)
        // We include emotions in the text prediction for better context
        val textForSentiment = "$journalText ${selectedEmotions.joinToString(" ")}"
        val sentiment = nbClassifier.predict(textForSentiment)

        // 2. Predict Category (Delegated to MulticlassSVMClassifier)
        var category = svmClassifier.predict(selectedEmotions, journalText)

        // --- SAFETY CHECK / CONSISTENCY ENFORCEMENT ---
        // This block prevents illogical combinations of Sentiment and Category.
        // For example, if Sentiment is clearly NEGATIVE, we shouldn't return a "Positive Focus" category.

        if (sentiment == Sentiment.NEGATIVE) {
            // If Negative, ban Positive categories.
            if (category == "High-Energy Positive Focus" || category == "Balanced and Contemplative") {
                category = "Processing Difficult Emotions"
            }
        }

        // Conversely, if sentiment is POSITIVE, don't allow "Difficult Emotions".
        if (sentiment == Sentiment.POSITIVE && category == "Processing Difficult Emotions") {
            category = "High-Energy Positive Focus"
        }

        // 3. Get Smart Reflection (Delegated to ReflectionData)
        // Instead of hardcoded strings, we now query our database of 50+ smart prompts
        // to find the one that best matches the user's emotions and keywords.
        val reflectionEntry = ReflectionData.getReflectionFor(selectedEmotions, sentiment, journalText)

        return AnalysisResult(
            sentiment = sentiment,
            overallMoodCategory = category,
            reflectionPrompt = reflectionEntry.prompt,
            suggestedMicroActions = listOf(MicroAction("Recommended", reflectionEntry.microAction))
        )
    }

    // --- UTILITIES ---

    // Used for the Stats screen to find frequent words (delegated to NaiveBayesClassifier)
    fun extractKeyWords(text: String): Pair<List<String>, List<String>> {
        return nbClassifier.identifyKeywords(text)
    }

    // Helper: Reads the training data JSON file from the app's assets.
    private fun loadJsonFromAssets(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }

    // Helper: Converts a JSON Array into a standard List<String>
    private fun jsonArrayToList(arr: JSONArray): List<String> {
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) out.add(arr.getString(i))
        return out
    }
}