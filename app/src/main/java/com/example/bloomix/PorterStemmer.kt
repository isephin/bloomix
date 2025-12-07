package com.example.bloomix

import java.util.Locale

/**
 * Porter Stemmer in Kotlin.
 * Reduces words to their root form (e.g. "running" -> "run", "happiness" -> "happi").
 * This improves the accuracy of the Naive Bayes classifier by grouping similar words.
 */
object PorterStemmer {
    // Main public function to stem a word.
    // It creates an instance of the inner Class, adds the word to its buffer, and calls stem().
    fun stem(word: String): String {
        val stemClass = Class()
        stemClass.add(word.toCharArray(), word.length)
        stemClass.stem()
        return stemClass.toString()
    }

    // Inner class that encapsulates the state and logic of the stemming algorithm.
    private class Class {
        private var b: CharArray = CharArray(0) // The buffer holding the word characters
        private var i = 0 // Current character index being processed
        private var i_end = 0 // Index of the last character in the buffer
        private var j = 0 // General purpose index used in various steps
        private var k = 0 // Index of the last character of the stem being formed

        // Adds characters to the buffer 'b'. Resizes the buffer if necessary.
        fun add(w: CharArray, wLen: Int) {
            if (i + wLen >= b.size) {
                val new_b = CharArray(i + wLen + INC)
                for (c in 0 until i) new_b[c] = b[c]
                b = new_b
            }
            for (c in 0 until wLen) b[i++] = w[c]
        }

        // Returns the stemmed word as a String.
        override fun toString(): String {
            return String(b, 0, i_end)
        }

        // The main stemming logic orchestrator.
        // It executes the 5 steps of the Porter Stemming algorithm sequentially.
        fun stem() {
            k = i - 1 // Set k to the last character index
            if (k > 1) { // Only stem if the word has more than 2 letters
                step1()
                step2()
                step3()
                step4()
                step5()
            }
            i_end = k + 1 // Set the end of the string to the new stem length
            i = 0 // Reset current index
        }

        // Helper: Checks if the character at index 'i' is a consonant.
        // 'y' is considered a consonant unless it is preceded by a consonant.
        private fun cons(i: Int): Boolean {
            return when (b[i]) {
                'a', 'e', 'i', 'o', 'u' -> false
                'y' -> if (i == 0) true else !cons(i - 1)
                else -> true
            }
        }

        // Helper: Measures the number of consonant sequences (m) in the word up to index 'k'.
        // Used to determine if a suffix can be removed.
        private fun m(): Int {
            var n = 0
            var i = 0
            while (true) {
                if (i > k) return n
                if (!cons(i)) break
                i++
            }
            i++
            while (true) {
                while (true) {
                    if (i > k) return n
                    if (cons(i)) break
                    i++
                }
                i++
                n++
                while (true) {
                    if (i > k) return n
                    if (!cons(i)) break
                    i++
                }
                i++
            }
        }

        // Helper: Checks if there is a vowel in the stem (up to index 'k').
        private fun vowelinstem(): Boolean {
            var i = 0
            while (i <= k) {
                if (!cons(i)) return true
                i++
            }
            return false
        }

        // Helper: Checks for a double consonant at index 'j' (e.g., 'tt', 'ss').
        private fun doublec(j: Int): Boolean {
            if (j < 1) return false
            return if (b[j] != b[j - 1]) false else cons(j)
        }

        // Helper: Checks for a CVC (Consonant-Vowel-Consonant) pattern ending at index 'i'.
        // The second consonant (at i) cannot be w, x, or y.
        private fun cvc(i: Int): Boolean {
            if (i < 2 || !cons(i) || cons(i - 1) || !cons(i - 2)) return false
            val ch = b[i].toInt()
            return if (ch == 'w'.toInt() || ch == 'x'.toInt() || ch == 'y'.toInt()) false else true
        }

        // Helper: Checks if the word ends with string 's'. Updates 'j' to point before the suffix.
        private fun ends(s: String): Boolean {
            val l = s.length
            val o = k - l + 1
            if (o < 0) return false
            for (i in 0 until l) if (b[o + i] != s[i]) return false
            j = k - l
            return true
        }

        // Helper: Sets the end of the word (suffix) to string 's'.
        private fun setto(s: String) {
            val l = s.length
            val o = j + 1
            for (i in 0 until l) b[o + i] = s[i]
            k = j + l
        }

        // Helper: Replaces the suffix with 's' if the measure (m) of the stem > 0.
        private fun r(s: String) {
            if (m() > 0) setto(s)
        }

        // Step 1: Handles plural forms and basic suffixes like -ed, -ing.
        // e.g., "caresses" -> "caress", "ponies" -> "poni", "feed" -> "feed", "agreed" -> "agree"
        private fun step1() {
            if (b[k] == 's') {
                if (ends("sses")) k -= 2 else if (ends("ies")) setto("i") else if (b[k - 1] != 's') k--
            }
            if (ends("eed")) {
                if (m() > 0) k--
            } else if ((ends("ed") || ends("ing")) && vowelinstem()) {
                k = j
                if (ends("at")) setto("ate") else if (ends("bl")) setto("ble") else if (ends("iz")) setto("ize") else if (doublec(k)) {
                    k--
                    val ch = b[k].toInt()
                    if (ch == 'l'.toInt() || ch == 's'.toInt() || ch == 'z'.toInt()) k++
                } else if (m() == 1 && cvc(k)) setto("e")
            }
        }

        // Step 2: Handles the termination of words with 'y'.
        // e.g., "happy" -> "happi" if there's a vowel before 'y'.
        private fun step2() {
            if (ends("y") && vowelinstem()) b[k] = 'i'
        }

        // Step 3: Handles double suffixes. Maps complex suffixes to simpler ones.
        // e.g., "ational" -> "ate", "tional" -> "tion", "izer" -> "ize"
        private fun step3() {
            if (k == 0) return
            when (b[k - 1]) {
                'a' -> {
                    if (ends("ational")) {
                        r("ate")
                        return
                    }
                    if (ends("tional")) {
                        r("tion")
                        return
                    }
                }
                'c' -> {
                    if (ends("enci")) {
                        r("ence")
                        return
                    }
                    if (ends("anci")) {
                        r("ance")
                        return
                    }
                }
                'e' -> if (ends("izer")) {
                    r("ize")
                    return
                }
                'l' -> {
                    if (ends("bli")) {
                        r("ble")
                        return
                    }
                    if (ends("alli")) {
                        r("al")
                        return
                    }
                    if (ends("entli")) {
                        r("ent")
                        return
                    }
                    if (ends("eli")) {
                        r("e")
                        return
                    }
                    if (ends("ousli")) {
                        r("ous")
                        return
                    }
                }
                'o' -> {
                    if (ends("ization")) {
                        r("ize")
                        return
                    }
                    if (ends("ation")) {
                        r("ate")
                        return
                    }
                    if (ends("ator")) {
                        r("ate")
                        return
                    }
                }
                's' -> {
                    if (ends("alism")) {
                        r("al")
                        return
                    }
                    if (ends("iveness")) {
                        r("ive")
                        return
                    }
                    if (ends("fulness")) {
                        r("ful")
                        return
                    }
                    if (ends("ousness")) {
                        r("ous")
                        return
                    }
                }
                't' -> {
                    if (ends("aliti")) {
                        r("al")
                        return
                    }
                    if (ends("iviti")) {
                        r("ive")
                        return
                    }
                    if (ends("biliti")) {
                        r("ble")
                        return
                    }
                }
                'g' -> if (ends("logi")) {
                    r("log")
                    return
                }
            }
        }

        // Step 4: Handles suffix removal for -ic, -full, -ness, etc.
        // e.g., "icate" -> "ic", "ness" -> ""
        private fun step4() {
            when (b[k]) {
                'e' -> {
                    if (ends("icate")) {
                        r("ic")
                        return
                    }
                    if (ends("ative")) {
                        r("")
                        return
                    }
                    if (ends("alize")) {
                        r("al")
                        return
                    }
                }
                'i' -> if (ends("iciti")) {
                    r("ic")
                    return
                }
                'l' -> {
                    if (ends("ical")) {
                        r("ic")
                        return
                    }
                    if (ends("ful")) {
                        r("")
                        return
                    }
                }
                's' -> if (ends("ness")) {
                    r("")
                    return
                }
            }
        }

        // Step 5: Handles cleanup of endings.
        // e.g., removes final 'e' if m > 1, or handles 'll' -> 'l'
        private fun step5() {
            if (k == 0) return
            when (b[k - 1]) {
                'a' -> if (ends("al")) return
                'c' -> if (ends("ance") || ends("ence")) return
                'e' -> if (ends("er")) return
                'i' -> if (ends("ic")) return
                'l' -> if (ends("able") || ends("ible")) return
                'n' -> if (ends("ant") || ends("ement") || ends("ment") || ends("ent")) return
                'o' -> if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) return else if (ends("ou")) return
                's' -> if (ends("ism")) return
                't' -> if (ends("ate") || ends("iti")) return
                'u' -> if (ends("ous")) return
                'v' -> if (ends("ive")) return
                'z' -> if (ends("ize")) return
            }
            if (m() > 1) k = j
        }

        private companion object {
            private const val INC = 50 // Increment size for buffer resizing
        }
    }
}