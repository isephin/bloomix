package com.example.bloomix

import kotlin.math.ln
import kotlin.math.exp
import kotlin.random.Random

// --- 1. NAIVE BAYES CLASSIFIER (For Sentiment) ---

class NaiveBayesClassifier {
    private var classCounts = mutableMapOf<Sentiment, Int>()
    private var wordCounts = mutableMapOf<Sentiment, MutableMap<String, Int>>()
    private var vocab = mutableSetOf<String>()
    private var totalDocs = 0

    fun train(text: String, label: Sentiment) {
        totalDocs++
        classCounts[label] = classCounts.getOrDefault(label, 0) + 1

        val words = text.lowercase().split("\\s+".toRegex())
        val sentimentMap = wordCounts.getOrPut(label) { mutableMapOf() }

        for (word in words) {
            if (word.length > 2) { // Filter tiny words
                vocab.add(word)
                sentimentMap[word] = sentimentMap.getOrDefault(word, 0) + 1
            }
        }
    }

    fun predict(text: String): Sentiment {
        var bestSentiment = Sentiment.NEUTRAL
        var maxLogProb = Double.NEGATIVE_INFINITY

        val words = text.lowercase().split("\\s+".toRegex())

        // Calculate probability for each class: P(Class | Words)
        for (sentiment in Sentiment.values()) {
            // P(Class)
            val docCount = classCounts[sentiment] ?: 0
            val prior = if (totalDocs > 0) ln(docCount.toDouble() / totalDocs) else 0.0

            // P(Words | Class)
            var logLikelihood = 0.0
            val sentimentWordCounts = wordCounts[sentiment]
            val totalWordsInClass = sentimentWordCounts?.values?.sum() ?: 0

            for (word in words) {
                if (vocab.contains(word)) {
                    val count = sentimentWordCounts?.get(word) ?: 0
                    // Laplace Smoothing (+1) to handle unknown words
                    val prob = (count + 1).toDouble() / (totalWordsInClass + vocab.size)
                    logLikelihood += ln(prob)
                }
            }

            val totalScore = prior + logLikelihood
            if (totalScore > maxLogProb) {
                maxLogProb = totalScore
                bestSentiment = sentiment
            }
        }
        return bestSentiment
    }
}

// --- 2. SUPPORT VECTOR MACHINE (SVM) (For Mood Category) ---

class LinearSVM(private val numFeatures: Int, private val learningRate: Double = 0.01) {
    private val weights = DoubleArray(numFeatures) // The "Hyperplane"
    private var bias = 0.0

    // Train on a single example (Stochastic Gradient Descent)
    fun train(features: DoubleArray, label: Int) {
        // Label should be -1 or 1
        val y = if (label == 1) 1.0 else -1.0
        val dotProduct = dot(weights, features) + bias

        // Hinge Loss condition: if prediction is wrong or within margin
        if (y * dotProduct < 1) {
            for (i in weights.indices) {
                weights[i] = weights[i] + learningRate * (y * features[i] - 0.01 * weights[i]) // Update rule with regularization
            }
            bias += learningRate * y
        } else {
            // Just regularization
            for (i in weights.indices) {
                weights[i] = weights[i] - learningRate * 0.01 * weights[i]
            }
        }
    }

    fun predict(features: DoubleArray): Double {
        return dot(weights, features) + bias
    }

    private fun dot(w: DoubleArray, x: DoubleArray): Double {
        var sum = 0.0
        for (i in w.indices) {
            sum += w[i] * x[i]
        }
        return sum
    }
}

/**
 * Helper to manage multiple SVMs for Multi-Class classification (One-vs-Rest)
 */
class MulticlassSVMClassifier(private val categories: List<String>) {
    // A dictionary mapping words to specific index positions for the feature vector
    private val featureIndex = mutableMapOf<String, Int>()
    private var featureCount = 0

    // One SVM per category
    private val models = mutableMapOf<String, LinearSVM>()

    fun buildVocabulary(allTexts: List<String>) {
        featureIndex.clear()
        featureCount = 0
        // Use emotions and common words as features
        val keywords = listOf(
            "happy", "sad", "angry", "tired", "bored", "confused", "loved", "calm", "shocked",
            "stressed", "annoyed", "excited", "work", "home", "sleep", "friend", "bad", "good"
        )
        for (word in keywords) {
            featureIndex[word] = featureCount++
        }

        // Initialize one model for each category
        categories.forEach { category ->
            models[category] = LinearSVM(featureCount)
        }
    }

    fun train(emotions: List<String>, text: String, targetCategory: String) {
        val features = extractFeatures(emotions, text)

        // Train each model: 1 if it matches the target, -1 if it doesn't
        categories.forEach { category ->
            val label = if (category == targetCategory) 1 else 0
            models[category]?.train(features, label)
        }
    }

    fun predict(emotions: List<String>, text: String): String {
        val features = extractFeatures(emotions, text)
        var bestCategory = categories.first()
        var maxScore = Double.NEGATIVE_INFINITY

        // Ask each SVM for its confidence score
        categories.forEach { category ->
            val score = models[category]?.predict(features) ?: 0.0
            if (score > maxScore) {
                maxScore = score
                bestCategory = category
            }
        }
        return bestCategory
    }

    private fun extractFeatures(emotions: List<String>, text: String): DoubleArray {
        val vector = DoubleArray(featureCount)

        // 1. Emotion features
        for (emo in emotions) {
            featureIndex[emo.lowercase()]?.let { idx -> vector[idx] = 1.0 }
        }

        // 2. Text features
        val words = text.lowercase().split("\\s+".toRegex())
        for (word in words) {
            featureIndex[word]?.let { idx -> vector[idx] = 1.0 }
        }
        return vector
    }
}