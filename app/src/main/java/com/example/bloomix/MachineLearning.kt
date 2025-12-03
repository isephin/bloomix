package com.example.bloomix

import kotlin.math.ln
import kotlin.math.sqrt

// -----------------------------
// TEXT PREPROCESSOR UTILITIES
// -----------------------------
object TextPreprocessor {
    // UPDATED: Added "with", "about", "from", "as", "if", "can" to prevent them from appearing as keywords
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

    private val emojiTokens = mapOf(
        "😃" to "emoji_happy", "😊" to "emoji_happy", "😀" to "emoji_happy",
        "😢" to "emoji_sad", "😭" to "emoji_very_sad",
        "😡" to "emoji_angry", "😠" to "emoji_angry",
        "😮" to "emoji_surprised", "😱" to "emoji_shocked",
        "😍" to "emoji_love", "❤️" to "emoji_love",
        "😴" to "emoji_tired", "😓" to "emoji_stressed"
    )

    private val punctTokens = mapOf(
        "!" to "exclaim", "!!" to "exclaim_multi",
        "?" to "question", "??" to "question_multi",
        "..." to "ellipsis"
    )

    fun tokenize(text: String): List<String> {
        val normalized = text
            .replace(Regex("https?://\\S+"), " ")
            .replace(Regex("@\\w+"), " ")
            .replace(Regex("[.,;:\\(\\)\\[\\]\"]"), " ")
            .trim()

        val punctFeatures = mutableListOf<String>()
        for ((k, v) in punctTokens) {
            if (text.contains(k)) punctFeatures.add(v)
        }

        var processed = normalized
        for ((emoji, token) in emojiTokens) {
            if (processed.contains(emoji)) {
                processed = processed.replace(emoji, " $token ")
            }
        }

        val rawWords = processed.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val out = mutableListOf<String>()
        var i = 0
        while (i < rawWords.size) {
            val w = rawWords[i]
            if (w in stopwords) {
                i++
                continue
            }
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
        out.addAll(punctFeatures)
        return out
    }
}

// -----------------------------
// 1) NAIVE BAYES CLASSIFIER
// -----------------------------
class NaiveBayesClassifier(
    private val minWordFreq: Int = 2
) {
    private var classDocCounts = mutableMapOf<Sentiment, Int>()
    private var classWordSums = mutableMapOf<Sentiment, MutableMap<String, Double>>()
    private var docFreq = mutableMapOf<String, Int>()
    private var totalDocs = 0
    private var vocab = mutableSetOf<String>()
    private var finalized = false

    private val docWordSets = mutableListOf<Set<String>>()
    private val trainingDocs = mutableListOf<Pair<String, Sentiment>>()

    fun train(text: String, label: Sentiment) {
        totalDocs++
        classDocCounts[label] = classDocCounts.getOrDefault(label, 0) + 1
        trainingDocs.add(text to label)

        val tokens = TextPreprocessor.tokenize(text)
        val docSet = tokens.toSet()
        docWordSets.add(docSet)

        for (w in docSet) {
            docFreq[w] = docFreq.getOrDefault(w, 0) + 1
        }
    }

    fun finalizeTraining() {
        // Changed to use the passed minWordFreq, though usually we might want to capture all for small datasets
        vocab = docFreq.filter { it.value >= minWordFreq }.keys.toMutableSet()

        for (s in Sentiment.values()) {
            classWordSums[s] = mutableMapOf()
        }

        for ((idx, pair) in trainingDocs.withIndex()) {
            val (text, label) = pair
            val tokens = TextPreprocessor.tokenize(text).filter { vocab.contains(it) }
            if (tokens.isEmpty()) continue

            val tf = mutableMapOf<String, Int>()
            for (t in tokens) tf[t] = tf.getOrDefault(t, 0) + 1

            for ((word, count) in tf) {
                val df = docFreq[word] ?: 1
                val idf = ln((1.0 + totalDocs) / (1.0 + df)) + 1.0
                val tfidf = count * idf
                val classMap = classWordSums[label]!!
                classMap[word] = classMap.getOrDefault(word, 0.0) + tfidf
            }
        }
        finalized = true
    }

    fun predict(text: String): Sentiment {
        if (!finalized) {
            finalizeTraining()
        }

        val tokens = TextPreprocessor.tokenize(text).filter { vocab.contains(it) }
        val qtf = mutableMapOf<String, Int>()
        for (t in tokens) qtf[t] = qtf.getOrDefault(t, 0) + 1

        val idfCache = mutableMapOf<String, Double>()
        for (w in qtf.keys) {
            val df = docFreq[w] ?: 1
            idfCache[w] = ln((1.0 + totalDocs) / (1.0 + df)) + 1.0
        }

        var bestSentiment = Sentiment.NEUTRAL
        var maxScore = Double.NEGATIVE_INFINITY

        for (sentiment in Sentiment.values()) {
            val prior = ln((classDocCounts.getOrDefault(sentiment, 0) + 1).toDouble() / (totalDocs + Sentiment.values().size.toDouble()))
            val classMap = classWordSums[sentiment] ?: mutableMapOf()
            val totalClassWeight = classMap.values.sum() + vocab.size * 1.0

            var logLikelihood = 0.0
            for ((word, count) in qtf) {
                val idf = idfCache[word] ?: 1.0
                val tfidf = count * idf
                val classWordWeight = classMap.getOrDefault(word, 0.0)
                val prob = (classWordWeight + 1.0) / (totalClassWeight + vocab.size)
                val weightFactor = tfidf
                logLikelihood += ln(prob) * weightFactor
            }

            val score = prior + logLikelihood
            if (score > maxScore) {
                maxScore = score
                bestSentiment = sentiment
            }
        }
        return bestSentiment
    }

    // NEW FEATURE: Identify which words in the text contributed to Positive or Negative scores
    fun identifyKeywords(text: String): Pair<List<String>, List<String>> {
        if (!finalized) finalizeTraining()

        val tokens = TextPreprocessor.tokenize(text).filter { vocab.contains(it) }
        val positives = mutableListOf<String>()
        val negatives = mutableListOf<String>()

        val posMap = classWordSums[Sentiment.POSITIVE] ?: mapOf()
        val negMap = classWordSums[Sentiment.NEGATIVE] ?: mapOf()
        // ADDED: Get Neutral map to avoid flagging neutral words as positive/negative
        val neuMap = classWordSums[Sentiment.NEUTRAL] ?: mapOf()

        for (token in tokens) {
            val posWeight = posMap[token] ?: 0.0
            val negWeight = negMap[token] ?: 0.0
            val neuWeight = neuMap[token] ?: 0.0

            // CHANGED: Logic is now more robust.
            // A word is Positive if it's stronger than Negative AND stronger than Neutral
            if (posWeight > negWeight * 1.3 && posWeight > neuWeight * 1.3) {
                positives.add(token)
            }
            // A word is Negative if it's stronger than Positive AND stronger than Neutral
            else if (negWeight > posWeight * 1.3 && negWeight > neuWeight * 1.3) {
                negatives.add(token)
            }
        }
        // Return distinct words to avoid repetition (e.g., "Hope, Hope, Hope" becomes just "Hope")
        return Pair(positives.distinct(), negatives.distinct())
    }
}

// -----------------------------
// 2) LINEAR SVM
// -----------------------------
class LinearSVM(private val numFeatures: Int, private val learningRate: Double = 0.01) {
    private val weights = DoubleArray(numFeatures) { 0.0 }
    private var bias = 0.0

    fun train(features: DoubleArray, label: Int) {
        val y = if (label == 1) 1.0 else -1.0
        val dotProduct = dot(weights, features) + bias

        val lambda = 0.01
        if (y * dotProduct < 1.0) {
            for (i in weights.indices) {
                weights[i] = weights[i] + learningRate * (y * features[i] - lambda * weights[i])
            }
            bias += learningRate * y
        } else {
            for (i in weights.indices) {
                weights[i] = weights[i] - learningRate * lambda * weights[i]
            }
        }
    }

    fun predict(features: DoubleArray): Double {
        return dot(weights, features) + bias
    }

    private fun dot(w: DoubleArray, x: DoubleArray): Double {
        var sum = 0.0
        for (i in w.indices) sum += w[i] * x[i]
        return sum
    }
}

// -----------------------------
// 3) MULTICLASS SVM MANAGER
// -----------------------------
class MulticlassSVMClassifier(private val categories: List<String>) {
    private val featureIndex = mutableMapOf<String, Int>()
    private var featureCount = 0
    private val models = mutableMapOf<String, LinearSVM>()
    private var built = false

    fun buildVocabulary(extraTexts: List<String> = emptyList()) {
        featureIndex.clear()
        featureCount = 0

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

        for (w in keywords + emojiTokens + specialTokens) {
            featureIndex[w] = featureCount++
        }

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

        models.clear()
        categories.forEach { cat ->
            models[cat] = LinearSVM(featureCount)
        }
        built = true
    }

    private fun extractFeatures(emotions: List<String>, text: String): DoubleArray {
        if (!built) buildVocabulary()

        val vec = DoubleArray(featureCount) { 0.0 }

        for (emo in emotions) {
            featureIndex[emo.lowercase()]?.let { idx -> vec[idx] = 1.0 }
        }

        val tokens = TextPreprocessor.tokenize(text)
        for (t in tokens) {
            featureIndex[t]?.let { idx ->
                vec[idx] = vec[idx] + 1.0
            }
        }

        val norm = sqrt(vec.fold(0.0) { acc, v -> acc + v * v })
        if (norm > 0.0) {
            for (i in vec.indices) vec[i] = vec[i] / norm
        }
        return vec
    }

    fun train(emotions: List<String>, text: String, targetCategory: String) {
        val features = extractFeatures(emotions, text)
        categories.forEach { category ->
            val label = if (category == targetCategory) 1 else -1
            models[category]?.train(features, label)
        }
    }

    fun trainEpochs(dataset: List<Triple<List<String>, String, String>>, epochs: Int = 3) {
        for (e in 0 until epochs) {
            val shuffled = dataset.shuffled()
            for ((emotions, text, target) in shuffled) {
                train(emotions, text, target)
            }
        }
    }

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