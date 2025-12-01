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
        // ==========================================
        // 1. TRAIN NAIVE BAYES (Sentiment Analysis)
        // ==========================================

        // --- POSITIVE ---
        naiveBayes.train("happy excited joy awesome great", Sentiment.POSITIVE)
        naiveBayes.train("loved blessed wonderful amazing best", Sentiment.POSITIVE)
        naiveBayes.train("productive energetic accomplished win success", Sentiment.POSITIVE)
        naiveBayes.train("peaceful calm relax content relief", Sentiment.POSITIVE)
        naiveBayes.train("hope future enjoy looking forward", Sentiment.POSITIVE)
        naiveBayes.train("grateful thankful appreciate lucky", Sentiment.POSITIVE)
        naiveBayes.train("almost done finished completed achievement", Sentiment.POSITIVE) // Fix for "almost done"
        naiveBayes.train("fun laugh smile playing friends", Sentiment.POSITIVE)

        // --- NEGATIVE ---
        naiveBayes.train("sad tired crying lonely depressed", Sentiment.NEGATIVE)
        naiveBayes.train("angry mad furious hate annoyed", Sentiment.NEGATIVE)
        naiveBayes.train("stressed overwhelmed panic anxious scared", Sentiment.NEGATIVE)
        naiveBayes.train("bad terrible awful horrible fail", Sentiment.NEGATIVE)
        naiveBayes.train("pain sick hurt headache broken", Sentiment.NEGATIVE)
        naiveBayes.train("bored stuck nothing empty dull", Sentiment.NEGATIVE)
        naiveBayes.train("minor inconveniences pissing me off", Sentiment.NEGATIVE)
        naiveBayes.train("doesn't work error bug fail crash", Sentiment.NEGATIVE)

        // --- NEUTRAL ---
        naiveBayes.train("okay fine average normal standard", Sentiment.NEUTRAL)
        naiveBayes.train("nothing special just day routine", Sentiment.NEUTRAL)
        naiveBayes.train("work sleep eat repeat bored tired", Sentiment.NEUTRAL)
        naiveBayes.train("confused unsure maybe perhaps", Sentiment.NEUTRAL)
        naiveBayes.train("bittersweet mixed complicated weird", Sentiment.NEUTRAL)


        // ==========================================
        // 2. TRAIN SVM (Mood Category)
        // ==========================================
        svm.buildVocabulary(listOf())

        // High Energy Positive (Active, Happy, Excited)
        svm.train(listOf("happy", "excited"), "best day ever energy", "High-Energy Positive Focus")
        svm.train(listOf("excited", "proud"), "achieved so much", "High-Energy Positive Focus")
        svm.train(listOf("happy", "loved"), "feeling great", "High-Energy Positive Focus")
        svm.train(listOf("shocked", "happy"), "surprise party", "High-Energy Positive Focus")

        // Balanced / Low Energy Positive (Calm, Loved, Grateful)
        svm.train(listOf("calm", "happy"), "peaceful day", "Balanced and Contemplative")
        svm.train(listOf("loved", "grateful"), "family time", "Balanced and Contemplative")
        svm.train(listOf("bored", "calm"), "just relaxing", "Balanced and Contemplative")
        svm.train(listOf("tired", "happy"), "good tired satisfied", "Balanced and Contemplative")

        // Negative / Difficult (Sad, Angry, Stressed)
        svm.train(listOf("sad", "tired"), "hard times crying", "Processing Difficult Emotions")
        svm.train(listOf("angry", "stressed"), "so mad at work", "Processing Difficult Emotions")
        svm.train(listOf("anxious", "scared"), "panic attack worry", "Processing Difficult Emotions")
        svm.train(listOf("annoyed", "bored"), "nothing to do hate it", "Processing Difficult Emotions")

        // Complex / Mixed (Conflicting emotions)
        svm.train(listOf("happy", "sad"), "bittersweet feeling", "Complex Emotional Landscape")
        svm.train(listOf("excited", "nervous"), "big changes coming", "Complex Emotional Landscape")
        svm.train(listOf("loved", "annoyed"), "family is hard but good", "Complex Emotional Landscape")
        svm.train(listOf("tired", "proud"), "worked hard exhausted", "Complex Emotional Landscape")
        svm.train(listOf("confused", "shocked"), "what happened", "Complex Emotional Landscape")
    }

    private fun generateReflection(sentiment: Sentiment, category: String, selectedEmotions: List<String>, journalText: String): Pair<String, String> {
        val bestMatch = ReflectionData.getReflectionFor(selectedEmotions, sentiment, journalText)
        return Pair(bestMatch.prompt, bestMatch.microAction)
    }

    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {

        // 1. ENRICH TEXT: Add the selected emotions to the text analysis
        // If you selected "Happy" 5 times, we add "happy happy happy happy happy" to the text.
        // This forces the Naive Bayes model to respect your button choices.
        val emotionText = selectedEmotions.joinToString(" ")
        val enrichedText = "$journalText $emotionText"

        // 2. PREDICT SENTIMENT (using enriched text)
        val sentiment = naiveBayes.predict(enrichedText)

        // 3. PREDICT CATEGORY
        val category = svm.predict(selectedEmotions, journalText)

        // 4. SANITY CHECK: If Sentiment and Category contradict, trust the Category (SVM)
        // because SVM considers both inputs more holistically.
        var finalSentiment = sentiment
        if (category == "High-Energy Positive Focus" && sentiment == Sentiment.NEGATIVE) {
            finalSentiment = Sentiment.POSITIVE // Override mismatch
        } else if (category == "Processing Difficult Emotions" && sentiment == Sentiment.POSITIVE) {
            finalSentiment = Sentiment.NEGATIVE // Override mismatch
        }

        val (prompt, microActionDesc) = generateReflection(finalSentiment, category, selectedEmotions, journalText)

        return AnalysisResult(
            sentiment = finalSentiment,
            overallMoodCategory = category,
            reflectionPrompt = prompt,
            suggestedMicroActions = listOf(MicroAction("General", microActionDesc))
        )
    }
}