package com.example.bloomix

import java.util.Locale

/**
 * Standard Porter Stemmer implementation to reduce words to their root form.
 * Example: "stressed" -> "stress", "running" -> "run"
 */
object PorterStemmer {
    fun stem(word: String): String {
        var s = word.lowercase(Locale.getDefault())
        if (s.length < 3) return s

        // Step 1a
        if (s.endsWith("sses")) s = s.substring(0, s.length - 2)
        else if (s.endsWith("ies")) s = s.substring(0, s.length - 2)
        else if (s.endsWith("ss")) {} // do nothing
        else if (s.endsWith("s")) s = s.substring(0, s.length - 1)

        // Step 1b
        if (s.endsWith("eed")) {
            if (m(s.substring(0, s.length - 3)) > 0) s = s.substring(0, s.length - 1)
        } else if ((s.endsWith("ed") && hasVowel(s.substring(0, s.length - 2))) ||
            (s.endsWith("ing") && hasVowel(s.substring(0, s.length - 3)))
        ) {
            s = if (s.endsWith("ed")) s.substring(0, s.length - 2) else s.substring(0, s.length - 3)
            if (s.endsWith("at") || s.endsWith("bl") || s.endsWith("iz")) {
                s += "e"
            } else if (isDoubleConsonant(s) && !s.endsWith("l") && !s.endsWith("s") && !s.endsWith("z")) {
                s = s.substring(0, s.length - 1)
            } else if (m(s) == 1 && isCvc(s)) {
                s += "e"
            }
        }

        // Step 1c
        if (s.endsWith("y") && hasVowel(s.substring(0, s.length - 1))) {
            s = s.substring(0, s.length - 1) + "i"
        }

        // Step 2 (Simplified for common cases)
        val suffixes = arrayOf(
            "ational" to "ate", "tional" to "tion", "enci" to "ence", "anci" to "ance",
            "izer" to "ize", "abli" to "able", "alli" to "al", "entli" to "ent",
            "eli" to "e", "ousli" to "ous", "ization" to "ize", "ation" to "ate",
            "ator" to "ate", "alism" to "al", "iveness" to "ive", "fulness" to "ful",
            "ousness" to "ous", "aliti" to "al", "iviti" to "ive", "biliti" to "ble"
        )
        for ((suffix, replacement) in suffixes) {
            if (s.endsWith(suffix)) {
                if (m(s.substring(0, s.length - suffix.length)) > 0) {
                    s = s.substring(0, s.length - suffix.length) + replacement
                }
                break
            }
        }

        // Step 3 & 4 (Simplified)
        if (s.endsWith("icate")) { if (m(s.substring(0, s.length - 5)) > 0) s = s.substring(0, s.length - 5) + "ic" }
        else if (s.endsWith("ative")) { if (m(s.substring(0, s.length - 5)) > 0) s = s.substring(0, s.length - 5) }
        else if (s.endsWith("alize")) { if (m(s.substring(0, s.length - 5)) > 0) s = s.substring(0, s.length - 5) + "al" }

        return s
    }

    private fun m(str: String): Int {
        var n = 0
        var isVowel = false
        for (i in str.indices) {
            if (isVowel(str[i])) {
                if (!isVowel) { n++; isVowel = true }
            } else {
                isVowel = false
            }
        }
        return n
    }

    private fun hasVowel(str: String): Boolean = str.any { isVowel(it) }
    private fun isVowel(c: Char): Boolean = "aeiou".contains(c)
    private fun isDoubleConsonant(str: String): Boolean =
        str.length >= 2 && str[str.length - 1] == str[str.length - 2] && !isVowel(str.last())

    private fun isCvc(str: String): Boolean {
        if (str.length < 3) return false
        val last = str.last()
        val secondLast = str[str.length - 2]
        val thirdLast = str[str.length - 3]
        return !isVowel(last) && last !in "wxy" && isVowel(secondLast) && !isVowel(thirdLast)
    }
}