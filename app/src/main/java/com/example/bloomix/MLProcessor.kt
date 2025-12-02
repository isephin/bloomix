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
        // --- POSITIVE ---
        naiveBayes.train("happy excited joy awesome great", Sentiment.POSITIVE)
        naiveBayes.train("loved blessed wonderful amazing best", Sentiment.POSITIVE)
        naiveBayes.train("productive energetic accomplished win success", Sentiment.POSITIVE)
        naiveBayes.train("peaceful calm relax content relief", Sentiment.POSITIVE)
        naiveBayes.train("hope future enjoy looking forward", Sentiment.POSITIVE)
        naiveBayes.train("grateful thankful appreciate lucky", Sentiment.POSITIVE)
        naiveBayes.train("almost done finished completed achievement", Sentiment.POSITIVE)
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

        // --- SVM ---
        svm.buildVocabulary(listOf())
        svm.train(listOf("happy", "excited"), "best day ever energy", "High-Energy Positive Focus")
        svm.train(listOf("excited", "loved"), "so much energy", "High-Energy Positive Focus")
        svm.train(listOf("calm", "happy"), "peaceful day", "Balanced and Contemplative")
        svm.train(listOf("loved", "grateful"), "family time", "Balanced and Contemplative")
        svm.train(listOf("sad", "tired"), "hard times crying", "Processing Difficult Emotions")
        svm.train(listOf("angry", "stressed"), "so mad at work", "Processing Difficult Emotions")
        svm.train(listOf("happy", "sad"), "bittersweet feeling", "Complex Emotional Landscape")
        svm.train(listOf("excited", "nervous"), "big changes coming", "Complex Emotional Landscape")
    }

    private fun generateReflection(sentiment: Sentiment, category: String, selectedEmotions: List<String>, journalText: String): Pair<String, String> {
        val bestMatch = ReflectionData.getReflectionFor(selectedEmotions, sentiment, journalText)
        return Pair(bestMatch.prompt, bestMatch.microAction)
    }

    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {

        val emotionText = selectedEmotions.joinToString(" ") { "$it $it $it" }
        val enrichedText = "$journalText $emotionText"

        val sentiment = naiveBayes.predict(enrichedText)
        val category = svm.predict(selectedEmotions, journalText)

        var finalSentiment = sentiment
        if (category == "High-Energy Positive Focus" && sentiment == Sentiment.NEGATIVE) {
            finalSentiment = Sentiment.POSITIVE
        } else if (category == "Processing Difficult Emotions" && sentiment == Sentiment.POSITIVE) {
            finalSentiment = Sentiment.NEGATIVE
        } else if (category == "Balanced and Contemplative" && sentiment == Sentiment.NEGATIVE) {
            finalSentiment = Sentiment.NEUTRAL
        }

        val (prompt, microActionDesc) = generateReflection(finalSentiment, category, selectedEmotions, journalText)

        return AnalysisResult(
            sentiment = finalSentiment,
            overallMoodCategory = category,
            reflectionPrompt = prompt,
            suggestedMicroActions = listOf(MicroAction("General", microActionDesc))
        )
    }

    // NEW: Extract keywords for statistics visualization
    fun extractKeyWords(text: String): Pair<List<String>, List<String>> {
        return naiveBayes.identifyKeywords(text)
    }
}