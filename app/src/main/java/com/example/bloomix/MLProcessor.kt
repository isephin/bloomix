package com.example.bloomix

object MLProcessor {

    private val naiveBayes = NaiveBayesClassifier()
    private val svm = MulticlassSVMClassifier(
        listOf("High-Energy Positive Focus", "Balanced and Contemplative", "Processing Difficult Emotions", "Complex Emotional Landscape")
    )

    init {
        trainModels()
    }

    private fun trainModels() {
        // --- POSITIVE DATA (Expanded) ---
        naiveBayes.train("I am so happy and excited today", Sentiment.POSITIVE)
        naiveBayes.train("Everything is going great love it", Sentiment.POSITIVE)
        naiveBayes.train("What a wonderful blessed day", Sentiment.POSITIVE)
        naiveBayes.train("I feel calm and loved", Sentiment.POSITIVE)
        // NEW WORDS ADDED HERE:
        naiveBayes.train("peaceful quiet relax enjoy hope future", Sentiment.POSITIVE)
        naiveBayes.train("grateful good nice amazing", Sentiment.POSITIVE)

        // --- NEGATIVE DATA ---
        naiveBayes.train("I am sad and tired", Sentiment.NEGATIVE)
        naiveBayes.train("This is terrible and bad", Sentiment.NEGATIVE)
        naiveBayes.train("I feel angry and stressed", Sentiment.NEGATIVE)
        naiveBayes.train("Bored and annoyed by everything", Sentiment.NEGATIVE)
        naiveBayes.train("minor inconveniences pissing me off", Sentiment.NEGATIVE)

        // --- NEUTRAL DATA ---
        naiveBayes.train("It was an okay day nothing special", Sentiment.NEUTRAL)
        naiveBayes.train("Just working and sleeping", Sentiment.NEUTRAL)
        naiveBayes.train("normal average fine", Sentiment.NEUTRAL)

        // ... (SVM training remains the same) ...
        svm.buildVocabulary(listOf())
        // (Keep your existing SVM training lines here)
        svm.train(listOf("happy", "excited"), "best day ever", "High-Energy Positive Focus")
        svm.train(listOf("sad", "tired"), "hard times", "Processing Difficult Emotions")
        // ...
    }

    // ... (Rest of the file remains the same) ...

    private fun generateReflection(sentiment: Sentiment, category: String, selectedEmotions: List<String>, journalText: String): Pair<String, String> {
        val bestMatch = ReflectionData.getReflectionFor(selectedEmotions, sentiment, journalText)
        return Pair(bestMatch.prompt, bestMatch.microAction)
    }

    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {
        val sentiment = naiveBayes.predict(journalText)
        // If sentiment is Neutral but we have strong positive words, force Positive
        val finalSentiment = if (sentiment == Sentiment.NEUTRAL &&
            (journalText.contains("peaceful") || journalText.contains("enjoy"))) {
            Sentiment.POSITIVE
        } else {
            sentiment
        }

        val category = svm.predict(selectedEmotions, journalText)
        val (prompt, microActionDesc) = generateReflection(finalSentiment, category, selectedEmotions, journalText)

        return AnalysisResult(
            sentiment = finalSentiment,
            overallMoodCategory = category,
            reflectionPrompt = prompt,
            suggestedMicroActions = listOf(MicroAction("General", microActionDesc))
        )
    }
}