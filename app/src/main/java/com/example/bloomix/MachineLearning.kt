package com.example.bloomix

import kotlin.math.ln
import kotlin.math.sqrt

// -----------------------------
// TEXT PREPROCESSOR UTILITIES
// -----------------------------
/**
 * Handles cleaning and preparing text before the AI analyzes it.
 * Raw text is messy; this object turns it into a clean list of "tokens" (words).
 */
object TextPreprocessor {

    // A set of common "filler" words (stopwords) that don't add emotional meaning.
    // We remove these so the model focuses on important words like "happy", "failed", "love".
    // UPDATED: Added context words like "with", "about", "from" to the exclusion list.
    private val stopwords = setOf(
        "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
        "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers",
        "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves",
        "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are",
        "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does",
        "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until",
        "while", "of", "at", "by", "for", "with", "about", "against", "between", "into",
        "through", "during", "before", "after", "above", "below", "to", "from", "up", "down",
        "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here",
        "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more",
        "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so",
        "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now",
        "d", "ll", "m", "o", "re", "ve", "y", "ain", "aren", "couldn", "didn", "doesn",
        "hadn", "hasn", "haven", "isn", "ma", "mightn", "mustn", "needn", "shan", "shouldn",
        "wasn", "weren", "won", "wouldn"
    )

    // Maps specific emojis to text tokens so the model can "read" them.
    // e.g., "😭" becomes "emoji_very_sad"
    private val emojiTokens = mapOf(
        "😃" to "emoji_happy", "😊" to "emoji_happy", "😀" to "emoji_happy",
        "😢" to "emoji_sad", "😭" to "emoji_very_sad",
        "😡" to "emoji_angry", "😠" to "emoji_angry",
        "😮" to "emoji_surprised", "😱" to "emoji_shocked",
        "😍" to "emoji_love", "❤️" to "emoji_love",
        "😴" to "emoji_tired", "😓" to "emoji_stressed"
    )

    // Maps punctuation to tokens to capture intensity (e.g., "!!" is stronger than ".")
    private val punctTokens = mapOf(
        "!" to "exclaim", "!!" to "exclaim_multi",
        "?" to "question", "??" to "question_multi",
        "..." to "ellipsis"
    )

    /**
     * Main function: Converts a raw sentence into a list of meaningful keywords.
     */
    fun tokenize(text: String): List<String> {
        // 1. Regex Cleanup: Remove URLs, @mentions, and standard punctuation characters
        val normalized = text
            .replace(Regex("https?://\\S+"), " ")
            .replace(Regex("@\\w+"), " ")
            .replace(Regex("[.,;:\\(\\)\\[\\]\"]"), " ")
            .trim()

        // 2. Extract punctuation features (intensity indicators)
        val punctFeatures = mutableListOf<String>()
        for ((k, v) in punctTokens) {
            if (text.contains(k)) punctFeatures.add(v)
        }

        // 3. Replace Emojis with text tokens
        var processed = normalized
        for ((emoji, token) in emojiTokens) {
            if (processed.contains(emoji)) {
                processed = processed.replace(emoji, " $token ")
            }
        }

        // 4. Split by whitespace and lowercase everything
        val rawWords = processed.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val out = mutableListOf<String>()
        var i = 0

        // 5. Iterate words to handle Stopwords and Negation (handling "not happy")
        while (i < rawWords.size) {
            val w = rawWords[i]

            // Skip filler words
            if (w in stopwords) {
                i++
                continue
            }

            // Handle Negation: If "not", "never", or "no" appears, combine it with the next word.
            // e.g., "not good" becomes "not_good" (which is negative, unlike "good")
            if (w == "not" || w == "never" || w == "no") {
                if (i + 1 < rawWords.size) {
                    val next = rawWords[i + 1]
                    if (next.length > 0) {
                        out.add("not_$next")
                        i += 2
                        continue
                    }
                }
            }
            out.add(w)
            i++
        }

        // Add the punctuation tokens back in
        out.addAll(punctFeatures)
        return out
    }
}

// -----------------------------
// 1) NAIVE BAYES CLASSIFIER
// -----------------------------
/**
 * A probabilistic classifier based on Bayes' Theorem.
 * It calculates P(Sentiment | Words) to determine if text is POSITIVE, NEGATIVE, or NEUTRAL.
 */
class NaiveBayesClassifier(
    private val minWordFreq: Int = 2 // Words must appear at least twice to be considered (reduces noise)
) {
    // Tracks how many documents belong to each class (Pos/Neg/Neu)
    private var classDocCounts = mutableMapOf<Sentiment, Int>()

    // Tracks the "weight" (TF-IDF score) of every word for each sentiment class
    private var classWordSums = mutableMapOf<Sentiment, MutableMap<String, Double>>()

    // Tracks how many documents contain a specific word (used for IDF calculation)
    private var docFreq = mutableMapOf<String, Int>()

    private var totalDocs = 0
    private var vocab = mutableSetOf<String>()
    private var finalized = false // Flag to check if training is complete

    // Temporary storage used during the training phase
    private val docWordSets = mutableListOf<Set<String>>()
    private val trainingDocs = mutableListOf<Pair<String, Sentiment>>()

    /** Step 1: Feed data into the model. */
    fun train(text: String, label: Sentiment) {
        totalDocs++
        classDocCounts[label] = classDocCounts.getOrDefault(label, 0) + 1
        trainingDocs.add(text to label)

        val tokens = TextPreprocessor.tokenize(text)
        val docSet = tokens.toSet()
        docWordSets.add(docSet)

        // Update document frequency for each unique word found
        for (w in docSet) {
            docFreq[w] = docFreq.getOrDefault(w, 0) + 1
        }
    }

    /** Step 2: Calculate math (TF-IDF) once all data is fed. */
    fun finalizeTraining() {
        // Filter out very rare words to reduce noise
        vocab = docFreq.filter { it.value >= minWordFreq }.keys.toMutableSet()

        for (s in Sentiment.values()) {
            classWordSums[s] = mutableMapOf()
        }

        // Iterate through all training docs to calculate weights
        for ((idx, pair) in trainingDocs.withIndex()) {
            val (text, label) = pair
            val tokens = TextPreprocessor.tokenize(text).filter { vocab.contains(it) }
            if (tokens.isEmpty()) continue

            // TF (Term Frequency): How often word appears in this sentence
            val tf = mutableMapOf<String, Int>()
            for (t in tokens) tf[t] = tf.getOrDefault(t, 0) + 1

            for ((word, count) in tf) {
                // IDF (Inverse Document Frequency): How rare is this word across all docs?
                // Rare words (like "terrible") are weighted higher than common words.
                val df = docFreq[word] ?: 1
                // Add 1.0 to avoid division by zero
                val idf = ln((1.0 + totalDocs) / (1.0 + df)) + 1.0
                val tfidf = count * idf

                // Add score to the appropriate Sentiment bucket
                val classMap = classWordSums[label]!!
                classMap[word] = classMap.getOrDefault(word, 0.0) + tfidf
            }
        }
        finalized = true
    }

    /** Step 3: Predict sentiment for new text. */
    fun predict(text: String): Sentiment {
        if (!finalized) {
            finalizeTraining()
        }

        val tokens = TextPreprocessor.tokenize(text).filter { vocab.contains(it) }
        val qtf = mutableMapOf<String, Int>()
        for (t in tokens) qtf[t] = qtf.getOrDefault(t, 0) + 1

        // Pre-calculate IDF for the query words
        val idfCache = mutableMapOf<String, Double>()
        for (w in qtf.keys) {
            val df = docFreq[w] ?: 1
            idfCache[w] = ln((1.0 + totalDocs) / (1.0 + df)) + 1.0
        }

        var bestSentiment = Sentiment.NEUTRAL
        var maxScore = Double.NEGATIVE_INFINITY

        // Calculate probability score for each sentiment (Positive, Negative, Neutral)
        for (sentiment in Sentiment.values()) {
            // Prior probability: P(Class)
            val prior = ln((classDocCounts.getOrDefault(sentiment, 0) + 1).toDouble() / (totalDocs + Sentiment.values().size.toDouble()))
            val classMap = classWordSums[sentiment] ?: mutableMapOf()
            val totalClassWeight = classMap.values.sum() + vocab.size * 1.0

            var logLikelihood = 0.0
            for ((word, count) in qtf) {
                val idf = idfCache[word] ?: 1.0
                val tfidf = count * idf
                val classWordWeight = classMap.getOrDefault(word, 0.0)

                // Additive Smoothing: P(Word | Class)
                val prob = (classWordWeight + 1.0) / (totalClassWeight + vocab.size)

                // Use Logarithm to prevent underflow (multiplying tiny decimals -> 0)
                logLikelihood += ln(prob) * tfidf
            }

            // Final Score = Prior + Likelihood
            val score = prior + logLikelihood
            if (score > maxScore) {
                maxScore = score
                bestSentiment = sentiment
            }
        }
        return bestSentiment
    }

    /**
     * Feature: Identifies which words contributed most to Positive or Negative scores.
     * Used for the Stats Screen (e.g., "Positive words: Hope, Love")
     */
    fun identifyKeywords(text: String): Pair<List<String>, List<String>> {
        if (!finalized) finalizeTraining()

        val tokens = TextPreprocessor.tokenize(text).filter { vocab.contains(it) }
        val positives = mutableListOf<String>()
        val negatives = mutableListOf<String>()

        val posMap = classWordSums[Sentiment.POSITIVE] ?: mapOf()
        val negMap = classWordSums[Sentiment.NEGATIVE] ?: mapOf()
        val neuMap = classWordSums[Sentiment.NEUTRAL] ?: mapOf()

        for (token in tokens) {
            val posWeight = posMap[token] ?: 0.0
            val negWeight = negMap[token] ?: 0.0
            val neuWeight = neuMap[token] ?: 0.0

            // Heuristic: A word is "Positive" if it is significantly stronger than Neg/Neu weights
            if (posWeight > negWeight * 1.3 && posWeight > neuWeight * 1.3) {
                positives.add(token)
            }
            // Heuristic: A word is "Negative" if it is significantly stronger than Pos/Neu weights
            else if (negWeight > posWeight * 1.3 && negWeight > neuWeight * 1.3) {
                negatives.add(token)
            }
        }
        // Return distinct words to avoid repetition
        return Pair(positives.distinct(), negatives.distinct())
    }
}

// -----------------------------
// 2) LINEAR SVM (Support Vector Machine)
// -----------------------------
/**
 * A binary classifier that finds the best dividing line (hyperplane) between two classes.
 * We use Stochastic Gradient Descent (SGD) to train it.
 */
class LinearSVM(private val numFeatures: Int, private val learningRate: Double = 0.01) {
    private val weights = DoubleArray(numFeatures) { 0.0 }
    private var bias = 0.0

    /**
     * Updates weights based on a single training example using Hinge Loss.
     * label: 1 (Positive class) or -1 (Negative class)
     */
    fun train(features: DoubleArray, label: Int) {
        val y = if (label == 1) 1.0 else -1.0
        val dotProduct = dot(weights, features) + bias

        val lambda = 0.01 // Regularization parameter

        // Hinge Loss Check: If the point is on the wrong side or within the margin...
        if (y * dotProduct < 1.0) {
            // Update weights to push the boundary correctly
            for (i in weights.indices) {
                weights[i] = weights[i] + learningRate * (y * features[i] - lambda * weights[i])
            }
            bias += learningRate * y
        } else {
            // Just apply regularization (decay weights slightly)
            for (i in weights.indices) {
                weights[i] = weights[i] - learningRate * lambda * weights[i]
            }
        }
    }

    /** Returns the raw score (distance from the hyperplane). */
    fun predict(features: DoubleArray): Double {
        return dot(weights, features) + bias
    }

    /** Helper: Vector Dot Product calculation */
    private fun dot(w: DoubleArray, x: DoubleArray): Double {
        var sum = 0.0
        for (i in w.indices) sum += w[i] * x[i]
        return sum
    }
}

// -----------------------------
// 3) MULTICLASS SVM MANAGER
// -----------------------------
/**
 * Since SVM is binary (yes/no), this class manages multiple SVMs (One-vs-Rest)
 * to handle multiple mood categories (e.g., "High Energy", "Contemplative", etc.).
 */
class MulticlassSVMClassifier(private val categories: List<String>) {
    private val featureIndex = mutableMapOf<String, Int>()
    private var featureCount = 0
    private val models = mutableMapOf<String, LinearSVM>()
    private var built = false

    /**
     * Creates the "Bag of Words" vocabulary from keywords and training text.
     */
    fun buildVocabulary(extraTexts: List<String> = emptyList()) {
        featureIndex.clear()
        featureCount = 0

        // Hardcoded keywords relevant to mood classification
        val keywords = listOf(
            "happy", "sad", "angry", "tired", "bored", "confused", "loved", "calm", "shocked",
            "stressed", "annoyed", "excited", "work", "home", "sleep", "friend", "bad", "good",
            "lmao", "omg", "ugh", "cringe", "lol", "hate", "love", "study", "exam", "deadline",
            "sick", "sick_of", "sick_with", "sick_and"
        )

        val emojiTokens = listOf(
            "emoji_happy", "emoji_sad", "emoji_very_sad", "emoji_angry", "emoji_surprised",
            "emoji_shocked", "emoji_love", "emoji_tired", "emoji_stressed"
        )

        val specialTokens = listOf("exclaim", "exclaim_multi", "question", "question_multi", "ellipsis")

        // Map every token to a unique integer index (0, 1, 2...)
        for (w in keywords + emojiTokens + specialTokens) {
            featureIndex[w] = featureCount++
        }

        // Add extra frequent words from the provided texts
        val freq = mutableMapOf<String, Int>()
        for (text in extraTexts) {
            for (t in TextPreprocessor.tokenize(text)) {
                freq[t] = freq.getOrDefault(t, 0) + 1
            }
        }
        freq.entries.sortedByDescending { it.value }.take(20).forEach { (token, _) ->
            if (!featureIndex.containsKey(token)) {
                featureIndex[token] = featureCount++
            }
        }

        // Initialize one LinearSVM for each category
        models.clear()
        categories.forEach { cat ->
            models[cat] = LinearSVM(featureCount)
        }
        built = true
    }

    /** Converts text and emotions into a numerical feature vector. */
    private fun extractFeatures(emotions: List<String>, text: String): DoubleArray {
        if (!built) buildVocabulary()

        val vec = DoubleArray(featureCount) { 0.0 }

        // Set features for selected emotions (e.g., if "happy" is selected, set index of "happy" to 1)
        for (emo in emotions) {
            featureIndex[emo.lowercase()]?.let { idx -> vec[idx] = 1.0 }
        }

        // Add features for words in the text
        val tokens = TextPreprocessor.tokenize(text)
        for (t in tokens) {
            featureIndex[t]?.let { idx ->
                vec[idx] = vec[idx] + 1.0
            }
        }

        // L2 Normalization (Scale vector so length = 1)
        // This helps the SVM converge faster.
        val norm = sqrt(vec.fold(0.0) { acc, v -> acc + v * v })
        if (norm > 0.0) {
            for (i in vec.indices) vec[i] = vec[i] / norm
        }
        return vec
    }

    /** Trains all SVM models against a specific example. */
    fun train(emotions: List<String>, text: String, targetCategory: String) {
        val features = extractFeatures(emotions, text)
        categories.forEach { category ->
            // If this is the target category, label is 1 (Positive). Otherwise -1 (Negative).
            val label = if (category == targetCategory) 1 else -1
            models[category]?.train(features, label)
        }
    }

    /** Runs training multiple times (epochs) over the dataset to improve accuracy. */
    fun trainEpochs(dataset: List<Triple<List<String>, String, String>>, epochs: Int = 3) {
        for (e in 0 until epochs) {
            val shuffled = dataset.shuffled() // Shuffle to prevent bias order
            for ((emotions, text, target) in shuffled) {
                train(emotions, text, target)
            }
        }
    }

    /** * Predicts the best category.
     * It asks every SVM model for a score, and picks the highest one.
     */
    fun predict(emotions: List<String>, text: String): String {
        val features = extractFeatures(emotions, text)
        var bestCategory = categories.first()
        var bestScore = Double.NEGATIVE_INFINITY

        for (cat in categories) {
            val score = models[cat]?.predict(features) ?: Double.NEGATIVE_INFINITY
            if (score > bestScore) {
                bestScore = score
                bestCategory = cat
            }
        }
        return bestCategory
    }
}